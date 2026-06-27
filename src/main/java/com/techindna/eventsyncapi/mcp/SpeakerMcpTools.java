package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerListResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateResponseDto;
import com.techindna.eventsyncapi.service.SpeakerService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.techindna.eventsyncapi.mcp.McpToolSupport.logger;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.pageOrDefault;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.parseUuid;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.run;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.sizeOrDefault;

@Component
@RequiredArgsConstructor
public class SpeakerMcpTools {

    private static final String IP = "127.0.0.1";
    private static final Logger LOG = logger(SpeakerMcpTools.class);

    private final SpeakerService speakerService;

    @Tool(name = "createSpeaker",
            description = """
                    Create a new speaker with profile data and optional external links.

                    Returns a confirmation including id, name, email, bio, and external links.

                    Validation rules — strict pass or call fails with 422:
                    • firstName and lastName: 1–50 chars, only a-z A-Z ' - and space. Must
                      start and end with a letter; no digits, no leading/trailing spaces.
                    • email: required, max 50 chars, must match local@domain.tld format using
                      only a-z A-Z 0-9 . _ - @ characters.
                    • bio: required, non-blank, max 1000 chars, only A-Za-z0-9.,;\"!'?- and space.
                    • profilePicture: required, must be an http(s):// URL (max 255 chars) using
                      only a-z A-Z 0-9 - ? . _ % & # / characters.
                    • externalLinks: optional list. Each link needs a name (same rules as
                      firstName/lastName) and a URL (same rules as profilePicture).

                    Conflicts: duplicate email → 409 "Email ... already exists."; duplicate
                    URL inside externalLinks → 409 "URL ... already exists.".
                    """)
    public String createSpeaker(
            @ToolParam(description = "First name of the speaker (max 50 characters)") String firstName,
            @ToolParam(description = "Last name of the speaker (max 50 characters)") String lastName,
            @ToolParam(description = "Email of the speaker (max 50 characters)") String email,
            @ToolParam(description = "URL of the speaker's profile picture") String profilePicture,
            @ToolParam(description = "Short bio of the speaker (max 1000 characters)") String bio,
            @ToolParam(description = "Optional external links, each as {\"name\": \"...\", \"url\": \"...\"}. Omit or pass empty list to skip.") String externalLinksJson
    ) {
        return run(LOG, "createSpeaker", () -> {
            SpeakerInputDto input = SpeakerInputDto.builder()
                    .firstName(firstName.strip())
                    .lastName(lastName.strip())
                    .email(email.strip())
                    .profilePicture(profilePicture.strip())
                    .bio(bio.strip())
                    .externalLinks(parseExternalLinks(externalLinksJson))
                    .build();
            SpeakerResponseDto speaker = speakerService.createSpeaker(input);
            return formatSpeakerResponse("Speaker created:", speaker);
        });
    }

    @Tool(name = "listSpeakers",
            description = """
                    List speakers with optional name search and pagination.

                    Returns speaker names, ids, and emails, or 'No speakers found.' if empty.

                    Validation rules — strict pass or call fails with 422:
                    • search: must contain only letters, digits, spaces, hyphens,
                      and apostrophes. Avoid commas, slashes, colons, or other punctuation.
                    """)
    public String listSpeakers(
            @ToolParam(description = "Optional search term to filter speakers by name") String search,
            @ToolParam(description = "Page number (optional, defaults to 1)") Integer page,
            @ToolParam(description = "Items per page (optional, defaults to 10)") Integer size
    ) {
        return run(LOG, "listSpeakers", () -> {
            SpeakerListResponseDto result = speakerService.getAllSpeakers(
                    pageOrDefault(page), sizeOrDefault(size), search, IP);
            if (result.getData() == null || result.getData().isEmpty()) {
                return "No speakers found.";
            }
            var sb = new StringBuilder();
            sb.append("Speakers (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var speaker : result.getData()) {
                sb.append("- ").append(speaker.getFirstName()).append(" ")
                        .append(speaker.getLastName())
                        .append(" (").append(speaker.getId()).append(")")
                        .append(" — ").append(speaker.getEmail())
                        .append("\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "getSpeaker",
            description = """
                    Get details of a specific speaker by UUID, including bio, external links,
                    and the sessions they are scheduled for.

                    Returns the speaker details or 'Speaker ... not found.' on 404.
                    """)
    public String getSpeaker(
            @ToolParam(description = "UUID of the speaker") String id
    ) {
        return run(LOG, "getSpeaker", () -> {
            SpeakerDetailResponseDto speaker = speakerService.getSpeakerById(parseUuid(id), IP);
            var sb = new StringBuilder(String.format("""
                            Speaker details:
                            - ID: %s
                            - Name: %s %s
                            - Bio: %s
                            - Profile picture: %s
                            """,
                    speaker.getId(), speaker.getFirstName(), speaker.getLastName(),
                    speaker.getBio(), speaker.getProfilePicture()));
            if (speaker.getExternalLinks() != null && !speaker.getExternalLinks().isEmpty()) {
                sb.append("- External links (").append(speaker.getExternalLinks().size()).append("):\n");
                for (var link : speaker.getExternalLinks()) {
                    sb.append("  · ").append(link.getName())
                            .append(" → ").append(link.getUrl()).append("\n");
                }
            } else {
                sb.append("- External links: none\n");
            }
            if (speaker.getSessions() != null && !speaker.getSessions().isEmpty()) {
                sb.append("- Sessions (").append(speaker.getSessions().size()).append("):\n");
                for (var session : speaker.getSessions()) {
                    sb.append("  · ").append(session.getTitle())
                            .append(" (").append(session.getId()).append(")\n");
                }
            } else {
                sb.append("- Sessions: none\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "updateSpeaker",
            description = """
                    Update an existing speaker. All five fields are required and replace the
                    current values. bio and profilePicture are still required on update.

                    Returns a confirmation including id, name, email, bio, and profile picture.

                    Validation rules — strict pass or call fails with 422:
                    • firstName and lastName: 1–50 chars, only a-z A-Z ' - and space. Must
                      start and end with a letter; no digits, no leading/trailing spaces.
                    • email: required, max 50 chars, must match local@domain.tld format using
                      only a-z A-Z 0-9 . _ - @ characters.
                    • bio: required, non-blank, max 1000 chars, only A-Za-z0-9.,;\"!'?- and space.
                    • profilePicture: required, must be an http(s):// URL (max 255 chars) using
                      only a-z A-Z 0-9 - ? . _ % & # / characters.

                    Conflicts: duplicate email → 409 "Email '...' already exists.".
                    404 if the speaker UUID does not exist.
                    """)
    public String updateSpeaker(
            @ToolParam(description = "UUID of the speaker to update") String id,
            @ToolParam(description = "New first name of the speaker (max 50 characters)") String firstName,
            @ToolParam(description = "New last name of the speaker (max 50 characters)") String lastName,
            @ToolParam(description = "New email of the speaker (max 50 characters)") String email,
            @ToolParam(description = "New profile picture URL") String profilePicture,
            @ToolParam(description = "New bio of the speaker (max 1000 characters)") String bio
    ) {
        return run(LOG, "updateSpeaker", () -> {
            SpeakerUpdateInputDto input = SpeakerUpdateInputDto.builder()
                    .firstName(firstName.strip())
                    .lastName(lastName.strip())
                    .email(email.strip())
                    .profilePicture(profilePicture.strip())
                    .bio(bio.strip())
                    .build();
            SpeakerUpdateResponseDto speaker = speakerService.updateSpeaker(parseUuid(id), input);
            return String.format("""
                            Speaker updated:
                            - ID: %s
                            - Name: %s %s
                            - Email: %s
                            - Bio: %s
                            - Profile picture: %s
                            """,
                    speaker.getId(), speaker.getFirstName(), speaker.getLastName(),
                    speaker.getEmail(), speaker.getBio(), speaker.getProfilePicture());
        });
    }

    @Tool(name = "deleteSpeaker",
            description = """
                    Delete a speaker by their UUID.

                    Returns a confirmation string. 404 if the speaker UUID does not exist.
                    """)
    public String deleteSpeaker(
            @ToolParam(description = "UUID of the speaker to delete") String id
    ) {
        return run(LOG, "deleteSpeaker", () -> {
            UUID speakerId = parseUuid(id);
            speakerService.deleteSpeaker(speakerId);
            return String.format("Speaker %s deleted.", speakerId);
        });
    }

    @Tool(name = "addSpeakerExternalLink",
            description = """
                    Add an external link to a speaker's profile.

                    Returns the full list of external links for the speaker after the add.

                    Validation rules — strict pass or call fails with 422:
                    • name: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces.
                    • url: must be an http(s):// URL (max 255 chars) using only a-z A-Z 0-9
                      - ? . _ % & # / characters.

                    Conflicts: duplicate URL → 409 "URL ... already exists.".
                    404 if the speaker UUID does not exist.
                    """)
    public String addSpeakerExternalLink(
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "Display name of the link (max 50 characters)") String name,
            @ToolParam(description = "URL of the link (http or https)") String url
    ) {
        return run(LOG, "addSpeakerExternalLink", () -> {
            UUID spId = parseUuid(speakerId);
            ExternalLinkDto link = ExternalLinkDto.builder()
                    .name(name.strip())
                    .url(url.strip())
                    .build();
            var links = speakerService.addExternalLink(spId, link);
            return formatExternalLinks("External links:", spId, links);
        });
    }

    @Tool(name = "updateSpeakerExternalLink",
            description = """
                    Update an existing external link of a speaker, identified by its current name.

                    Returns the full list of external links for the speaker after the update.

                    Validation rules — strict pass or call fails with 422:
                    • urlName: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces. Must match the existing
                      link's name exactly.
                    • name: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces.
                    • url: must be an http(s):// URL (max 255 chars) using only a-z A-Z 0-9
                      - ? . _ % & # / characters.

                    Conflicts: duplicate URL → 409 "URL ... already exists.".
                    404 if the speaker UUID or the urlName link does not exist.
                    """)
    public String updateSpeakerExternalLink(
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "Current name of the link to update") String urlName,
            @ToolParam(description = "New display name for the link (max 50 characters)") String name,
            @ToolParam(description = "New URL for the link (http or https)") String url
    ) {
        return run(LOG, "updateSpeakerExternalLink", () -> {
            UUID spId = parseUuid(speakerId);
            ExternalLinkDto link = ExternalLinkDto.builder()
                    .name(name.strip())
                    .url(url.strip())
                    .build();
            var links = speakerService.updateExternalLink(spId, urlName.strip(), link);
            return formatExternalLinks("External links:", spId, links);
        });
    }

    @Tool(name = "deleteSpeakerExternalLink",
            description = """
                    Delete an external link of a speaker by its UUID.

                    Returns a confirmation string. 404 if the speaker UUID or externalLinkId
                    does not exist.
                    """)
    public String deleteSpeakerExternalLink(
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "UUID of the external link to delete") String externalLinkId
    ) {
        return run(LOG, "deleteSpeakerExternalLink", () -> {
            UUID spId = parseUuid(speakerId);
            UUID linkId = parseUuid(externalLinkId);
            speakerService.deleteExternalLink(spId, linkId);
            return String.format("External link %s deleted from speaker %s.", linkId, spId);
        });
    }

    private static String formatSpeakerResponse(String header, SpeakerResponseDto speaker) {
        var sb = new StringBuilder(String.format("""
                        %s
                        - ID: %s
                        - Name: %s %s
                        - Email: %s
                        """,
                header, speaker.getId(),
                speaker.getFirstName(), speaker.getLastName(),
                speaker.getEmail()));
        if (speaker.getExternalLinks() != null && !speaker.getExternalLinks().isEmpty()) {
            sb.append("- External links (").append(speaker.getExternalLinks().size()).append("):\n");
            for (var link : speaker.getExternalLinks()) {
                sb.append("  · ").append(link.getName())
                        .append(" → ").append(link.getUrl()).append("\n");
            }
        }
        return sb.toString();
    }

    private static String formatExternalLinks(String header, UUID speakerId, java.util.List<ExternalLinkDto> links) {
        if (links == null || links.isEmpty()) {
            return String.format("%s%n- Speaker %s has no external links.", header, speakerId);
        }
        var sb = new StringBuilder(header).append("\n");
        for (var link : links) {
            sb.append("- ").append(link.getName())
                    .append(" → ").append(link.getUrl()).append("\n");
        }
        return sb.toString();
    }

    private static java.util.List<ExternalLinkDto> parseExternalLinks(String json) {
        if (json == null || json.isBlank() || "[]".equals(json.strip())) {
            return java.util.Collections.emptyList();
        }
        try {
            var mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            var type = mapper.getTypeFactory().constructCollectionType(
                    java.util.List.class, ExternalLinkDto.class);
            return mapper.readValue(json, type);
        } catch (com.fasterxml.jackson.core.JsonProcessingException e) {
            throw new RuntimeException(
                    String.format("invalid externalLinks JSON: '%s'", json));
        }
    }
}

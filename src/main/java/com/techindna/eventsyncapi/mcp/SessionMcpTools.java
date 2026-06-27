package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionListResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

import static com.techindna.eventsyncapi.mcp.McpToolSupport.intOrDefault;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.logger;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.pageOrDefault;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.parseInstant;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.parseUuid;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.run;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.sizeOrDefault;

@Component
@RequiredArgsConstructor
public class SessionMcpTools {

    private static final String IP = "127.0.0.1";
    private static final Logger LOG = logger(SessionMcpTools.class);

    private final SessionService sessionService;

    @Tool(name = "createSession",
            description = """
                    Create a new session within an event.

                    Returns a confirmation including id, title, dates, room, event, and live status.

                    Validation rules — strict pass or call fails with 422:
                    • title: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces.
                    • description: non-blank, only A-Za-z0-9.,;"!'-
                    • startDate and endDate: required, endDate MUST be strictly after startDate.
                    • capacity: positive integer (e.g. 50). If omitted the call fails — pass an explicit value.
                    • roomId and eventId: must reference existing rows, otherwise 404.

                    Conflicts: duplicate title → 409 "Session '...' already exists.".

                    Example JSON for date fields (ISO-8601 instant):
                    ```json
                    {"startDate": "2026-07-15T14:00:00Z", "endDate": "2026-07-15T15:30:00Z"}
                    ```
                    """)
    public String createSession(
            @ToolParam(description = "Title of the session (max 100 characters)") String title,
            @ToolParam(description = "Description of the session (max 1000 characters)") String description,
            @ToolParam(description = "Start date/time in ISO-8601 format, e.g. 2026-07-15T14:00:00Z") String startDate,
            @ToolParam(description = "End date/time in ISO-8601 format, e.g. 2026-07-15T15:30:00Z") String endDate,
            @ToolParam(description = "UUID of the room where the session takes place") String roomId,
            @ToolParam(description = "Maximum capacity of the session (optional, defaults to 0)") Integer capacity,
            @ToolParam(description = "UUID of the parent event") String eventId
    ) {
        return run(LOG, "createSession", () -> {
            SessionInputDto input = SessionInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(parseInstant(startDate))
                    .endDate(parseInstant(endDate))
                    .roomId(parseUuid(roomId))
                    .capacity(intOrDefault(capacity, 0))
                    .eventId(parseUuid(eventId))
                    .build();
            SessionResponseDto session = sessionService.createSession(input);
            return formatSessionFields(
                    "Session created:",
                    session.getId(), session.getTitle(),
                    session.getRoom() != null ? session.getRoom().getName() : "N/A",
                    session.getEvent() != null ? session.getEvent().getTitle() : "N/A",
                    session.getStartDate(), session.getEndDate(),
                    session.getCapacity(), session.isLive());
        });
    }

    @Tool(name = "listSessions",
            description = """
                    List sessions with optional filters and pagination. Filters can be combined
                    to narrow results by room name, event title, or speaker name.

                    Returns a list of session titles, ids, room, event, and live status,
                    or 'No sessions found.' if empty.

                    Validation rules — strict pass or call fails with 422:
                    • room, event, speaker filters: each must contain only letters, digits,
                      spaces, hyphens, and apostrophes. Avoid commas, slashes, colons,
                      or other punctuation in the filter values.
                    """)
    public String listSessions(
            @ToolParam(description = "Optional filter by room name (case-insensitive partial match)") String room,
            @ToolParam(description = "Optional filter by event title (case-insensitive partial match)") String event,
            @ToolParam(description = "Optional filter by speaker name (case-insensitive partial match)") String speaker,
            @ToolParam(description = "Optional filter: true for currently live sessions only") Boolean live,
            @ToolParam(description = "Page number (optional, defaults to 1)") Integer page,
            @ToolParam(description = "Items per page (optional, defaults to 10)") Integer size
    ) {
        return run(LOG, "listSessions", () -> {
            SessionListResponseDto result = sessionService.getAllSessions(
                    pageOrDefault(page), sizeOrDefault(size),
                    room, event, speaker, live, IP);
            if (result.getData().isEmpty()) {
                return "No sessions found.";
            }
            var sb = new StringBuilder();
            sb.append("Sessions (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var session : result.getData()) {
                sb.append("- ").append(session.getTitle())
                        .append(" (").append(session.getId()).append(")")
                        .append(" — Room: ").append(session.getRoom() != null ? session.getRoom().getName() : "N/A")
                        .append(", Event: ").append(session.getEvent() != null ? session.getEvent().getTitle() : "N/A")
                        .append(session.isLive() ? " [LIVE]" : "")
                        .append("\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "getSession",
            description = """
                    Get details of a specific session by its UUID, including its room, event, speakers, and questions.

                    Returns the session details and any associated speakers or questions.
                    """)
    public String getSession(
            @ToolParam(description = "UUID of the session") String id
    ) {
        return run(LOG, "getSession", () -> {
            SessionDetailResponseDto session = sessionService.getSessionById(parseUuid(id), IP);
            var sb = new StringBuilder(formatSessionFields(
                    "Session details:",
                    session.getId(), session.getTitle(),
                    session.getRoom() != null ? session.getRoom().getName() : "N/A",
                    session.getEvent() != null ? session.getEvent().getTitle() : "N/A",
                    session.getStartDate(), session.getEndDate(),
                    session.getCapacity(), session.isLive()));
            if (session.getSpeakers() != null && !session.getSpeakers().isEmpty()) {
                sb.append("- Speakers (").append(session.getSpeakers().size()).append("):\n");
                for (var speaker : session.getSpeakers()) {
                    sb.append("  · ").append(speaker.getFirstName()).append(" ")
                            .append(speaker.getLastName())
                            .append(" (").append(speaker.getId()).append(")\n");
                }
            } else {
                sb.append("- Speakers: none\n");
            }
            if (session.getQuestions() != null && !session.getQuestions().isEmpty()) {
                sb.append("- Questions (").append(session.getQuestions().size()).append("):\n");
                for (var question : session.getQuestions()) {
                    sb.append("  · ").append(question.getTitle())
                            .append(" (").append(question.getUpvotes()).append(" upvotes)\n");
                }
            } else {
                sb.append("- Questions: none\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "updateSession",
            description = """
                    Update an existing session. All fields are required and will replace the current values.

                    Returns a confirmation including id, title, dates, room, event, and live status.

                    Validation rules — strict pass or call fails with 422:
                    • title: 1–50 chars, only a-z A-Z ' - and space. Must start and end with a
                      letter; no digits, no leading/trailing spaces.
                    • description: non-blank, only A-Za-z0-9.,;"!'-
                    • startDate and endDate: required, endDate MUST be strictly after startDate.
                    • capacity: positive integer. If omitted the call fails.
                    • roomId and eventId: must reference existing rows, otherwise 404.

                    Conflicts: duplicate title → 409 "Session '...' already exists.".

                    Example JSON for date fields (ISO-8601 instant):
                    ```json
                    {"startDate": "2026-08-01T10:00:00Z", "endDate": "2026-08-01T11:30:00Z"}
                    ```
                    """)
    public String updateSession(
            @ToolParam(description = "UUID of the session to update") String id,
            @ToolParam(description = "New title of the session (max 100 characters)") String title,
            @ToolParam(description = "New description of the session (max 1000 characters)") String description,
            @ToolParam(description = "New start date/time in ISO-8601 format, e.g. 2026-08-01T10:00:00Z") String startDate,
            @ToolParam(description = "New end date/time in ISO-8601 format, e.g. 2026-08-01T11:30:00Z") String endDate,
            @ToolParam(description = "New UUID of the room") String roomId,
            @ToolParam(description = "New maximum capacity of the session (optional, defaults to 0)") Integer capacity,
            @ToolParam(description = "New UUID of the parent event") String eventId
    ) {
        return run(LOG, "updateSession", () -> {
            SessionUpdateInputDto input = SessionUpdateInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(parseInstant(startDate))
                    .endDate(parseInstant(endDate))
                    .roomId(parseUuid(roomId))
                    .capacity(intOrDefault(capacity, 0))
                    .eventId(parseUuid(eventId))
                    .build();
            SessionResponseDto session = sessionService.updateSession(parseUuid(id), input);
            return formatSessionFields(
                    "Session updated:",
                    session.getId(), session.getTitle(),
                    session.getRoom() != null ? session.getRoom().getName() : "N/A",
                    session.getEvent() != null ? session.getEvent().getTitle() : "N/A",
                    session.getStartDate(), session.getEndDate(),
                    session.getCapacity(), session.isLive());
        });
    }

    @Tool(name = "deleteSession",
            description = """
                    Delete a session by its UUID.

                    Returns a confirmation string.
                    """)
    public String deleteSession(
            @ToolParam(description = "UUID of the session to delete") String id
    ) {
        return run(LOG, "deleteSession", () -> {
            UUID sessionId = parseUuid(id);
            sessionService.deleteSession(sessionId);
            return String.format("Session %s deleted.", sessionId);
        });
    }

    @Tool(name = "addSpeakerToSession",
            description = """
                    Add a speaker to a session with a time slot. The speaker will be linked to the session for the specified time range.

                    Returns a confirmation string.

                    Validation rules — strict pass or call fails with 422:
                    • startTime and endTime: required, ISO-8601 with timezone (e.g. 2026-07-15T14:00:00Z or 2026-07-15T14:00:00+00:00).
                    • endTime MUST be strictly after startTime.

                    Conflicts: if any speaker already occupies this session's room during the requested slot → 409 "The room is already occupied during the requested time slot.".
                    404 if the session or speaker UUID does not exist.
                    """)
    public String addSpeakerToSession(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "Start time of the speaker's slot, e.g. 2026-07-15T14:00:00Z") String startTime,
            @ToolParam(description = "End time of the speaker's slot, e.g. 2026-07-15T15:00:00Z") String endTime
    ) {
        return run(LOG, "addSpeakerToSession", () -> {
            UUID sId = parseUuid(sessionId);
            UUID spId = parseUuid(speakerId);
            SessionSpeakerInputDto input = SessionSpeakerInputDto.builder()
                    .startTime(parseInstant(startTime).toString())
                    .endTime(parseInstant(endTime).toString())
                    .build();
            String result = sessionService.addSpeakerToSession(sId, spId, input);
            return String.format("Speaker %s added to session %s. %s", spId, sId, result);
        });
    }

    @Tool(name = "updateSessionSpeakerLink",
            description = """
                    Update a speaker's time slot in a session.

                    Returns a confirmation string.

                    Validation rules — strict pass or call fails with 422:
                    • startTime and endTime: required, ISO-8601 with timezone (e.g. 2026-07-15T16:00:00Z).
                    • endTime MUST be strictly after startTime.

                    404 if linkId does not exist for the given (sessionId, speakerId) pair.
                    409 if the new slot overlaps another speaker in the same room.
                    """)
    public String updateSessionSpeakerLink(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "UUID of the session-speaker link to update") String linkId,
            @ToolParam(description = "New start time of the speaker's slot, e.g. 2026-07-15T16:00:00Z") String startTime,
            @ToolParam(description = "New end time of the speaker's slot, e.g. 2026-07-15T17:00:00Z") String endTime
    ) {
        return run(LOG, "updateSessionSpeakerLink", () -> {
            UUID sId = parseUuid(sessionId);
            UUID spId = parseUuid(speakerId);
            UUID lId = parseUuid(linkId);
            SessionSpeakerInputDto input = SessionSpeakerInputDto.builder()
                    .startTime(parseInstant(startTime).toString())
                    .endTime(parseInstant(endTime).toString())
                    .build();
            String result = sessionService.updateSessionSpeakerLink(sId, spId, lId, input);
            return String.format("Session-speaker link %s updated (speaker %s in session %s). %s",
                    lId, spId, sId, result);
        });
    }

    @Tool(name = "deleteSpeakerFromSession",
            description = """
                    Remove a speaker from a session by their UUIDs.

                    Returns a confirmation string.
                    """)
    public String deleteSpeakerFromSession(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker to remove") String speakerId
    ) {
        return run(LOG, "deleteSpeakerFromSession", () -> {
            UUID sId = parseUuid(sessionId);
            UUID spId = parseUuid(speakerId);
            sessionService.deleteSpeakerFromSession(sId, spId);
            return String.format("Speaker %s removed from session %s.", spId, sId);
        });
    }

    @Tool(name = "getSessionSpeakerTimeSlots",
            description = """
                    Get time slots for a speaker in a session.

                    Returns the link id, start time, and end time for each slot,
                    or 'No time slots found.' if the speaker has none in this session.
                    """)
    public String getSessionSpeakerTimeSlots(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker") String speakerId
    ) {
        return run(LOG, "getSessionSpeakerTimeSlots", () -> {
            UUID sId = parseUuid(sessionId);
            UUID spId = parseUuid(speakerId);
            var slots = sessionService.getSessionSpeakerTimeSlots(sId, spId, IP);
            if (slots == null || slots.isEmpty()) {
                return String.format("No time slots found for speaker %s in session %s.", spId, sId);
            }
            var sb = new StringBuilder();
            sb.append("Time slots for speaker ").append(spId)
                    .append(" in session ").append(sId).append(":\n");
            for (var slot : slots) {
                sb.append("- Link ID: ").append(slot.getId())
                        .append(", Start: ").append(slot.getStartTime())
                        .append(", End: ").append(slot.getEndTime())
                        .append("\n");
            }
            return sb.toString();
        });
    }

    private static String formatSessionFields(
            String header, UUID id, String title,
            String roomName, String eventTitle,
            Instant startDate, Instant endDate, int capacity, boolean live) {
        return String.format("""
                        %s
                        - ID: %s
                        - Title: %s
                        - Room: %s
                        - Event: %s
                        - Start: %s
                        - End: %s
                        - Capacity: %d
                        - Live: %s
                        """,
                header, id, title, roomName, eventTitle,
                startDate, endDate, capacity, live);
    }
}

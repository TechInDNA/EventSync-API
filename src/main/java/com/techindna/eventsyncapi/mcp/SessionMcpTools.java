package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionListResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.service.SessionService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionMcpTools {

    private final SessionService sessionService;

    @Tool(description = """
            Create a new session within an event. Returns the created session id, title, dates, room, event, and speakers.
            
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
        try {
            int cap = capacity != null ? capacity : 0;
            SessionInputDto input = SessionInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(Instant.parse(startDate))
                    .endDate(Instant.parse(endDate))
                    .roomId(UUID.fromString(roomId))
                    .capacity(cap)
                    .eventId(UUID.fromString(eventId))
                    .build();
            SessionResponseDto session = sessionService.createSession(input);
            return formatSessionResponse("Session created:", session);
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = """
            List sessions with optional filters and pagination. Returns session titles, IDs, room, event, and live status.
            Filters can be combined to narrow results by room name, event title, or speaker name.
            """)
    public String listSessions(
            @ToolParam(description = "Optional filter by room name (case-insensitive partial match)") String room,
            @ToolParam(description = "Optional filter by event title (case-insensitive partial match)") String event,
            @ToolParam(description = "Optional filter by speaker name (case-insensitive partial match)") String speaker,
            @ToolParam(description = "Optional filter: true for currently live sessions only") Boolean live,
            @ToolParam(description = "Page number (optional, defaults to 1)") Integer page,
            @ToolParam(description = "Items per page (optional, defaults to 10)") Integer size
    ) {
        try {
            int p = page != null ? page : 1;
            int s = size != null ? size : 10;
            SessionListResponseDto result = sessionService.getAllSessions(p, s, room, event,
                    speaker, live, "127.0.0.1");
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
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = """
            Get details of a specific session by its UUID, including its room, event, speakers, and questions.
            """)
    public String getSession(
            @ToolParam(description = "UUID of the session") String id
    ) {
        try {
            SessionDetailResponseDto session = sessionService.getSessionById(UUID.fromString(id), "127.0.0.1");
            var sb = new StringBuilder(
                    """
                    Session details:
                    - ID: %s
                    - Title: %s
                    - Description: %s
                    - Start: %s
                    - End: %s
                    - Capacity: %d
                    - Room: %s
                    - Event: %s
                    - Live: %s
                    """.formatted(
                    session.getId(), session.getTitle(), session.getDescription(),
                    session.getStartDate(), session.getEndDate(), session.getCapacity(),
                    session.getRoom() != null ? session.getRoom().getName() : "N/A",
                    session.getEvent() != null ? session.getEvent().getTitle() : "N/A",
                    session.isLive()
            ));
            if (session.getSpeakers() != null && !session.getSpeakers().isEmpty()) {
                sb.append("- Speakers (").append(session.getSpeakers().size()).append("):\n");
                for (var speaker : session.getSpeakers()) {
                    sb.append("  · ").append(speaker.getFirstName()).append(" ")
                            .append(speaker.getLastName())
                            .append(" (").append(speaker.getId()).append(")")
                            .append("\n");
                }
            } else {
                sb.append("- Speakers: none\n");
            }
            if (session.getQuestions() != null && !session.getQuestions().isEmpty()) {
                sb.append("- Questions (").append(session.getQuestions().size()).append("):\n");
                for (var question : session.getQuestions()) {
                    sb.append("  · ").append(question.getTitle())
                            .append(" (").append(question.getUpvotes()).append(" upvotes)")
                            .append("\n");
                }
            } else {
                sb.append("- Questions: none\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = """
            Update an existing session. All fields are required and will replace the current values.
            
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
        try {
            int cap = capacity != null ? capacity : 0;
            SessionUpdateInputDto input = SessionUpdateInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(Instant.parse(startDate))
                    .endDate(Instant.parse(endDate))
                    .roomId(UUID.fromString(roomId))
                    .capacity(cap)
                    .eventId(UUID.fromString(eventId))
                    .build();
            SessionResponseDto session = sessionService.updateSession(UUID.fromString(id), input);
            return formatSessionResponse("Session updated:", session);
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = "Delete a session by its UUID.")
    public String deleteSession(
            @ToolParam(description = "UUID of the session to delete") String id
    ) {
        try {
            sessionService.deleteSession(UUID.fromString(id));
            return String.format("Session %s deleted.", id);
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = """
            Add a speaker to a session with a time slot. The speaker will be linked to the session for the specified time range.
            Returns a confirmation message.
            """)
    public String addSpeakerToSession(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "Start time of the speaker's slot, e.g. 2026-07-15T14:00:00Z") String startTime,
            @ToolParam(description = "End time of the speaker's slot, e.g. 2026-07-15T15:00:00Z") String endTime
    ) {
        try {
            SessionSpeakerInputDto input = SessionSpeakerInputDto.builder()
                    .startTime(startTime.strip())
                    .endTime(endTime.strip())
                    .build();
            String result = sessionService.addSpeakerToSession(
                    UUID.fromString(sessionId), UUID.fromString(speakerId), input);
            return String.format("Speaker %s added to session %s. %s", speakerId, sessionId, result);
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = """
            Update a speaker's time slot in a session. Provide the session, speaker, and link IDs along with new start/end times.
            Returns a confirmation message.
            """)
    public String updateSessionSpeakerLink(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker") String speakerId,
            @ToolParam(description = "UUID of the session-speaker link to update") String linkId,
            @ToolParam(description = "New start time of the speaker's slot, e.g. 2026-07-15T16:00:00Z") String startTime,
            @ToolParam(description = "New end time of the speaker's slot, e.g. 2026-07-15T17:00:00Z") String endTime
    ) {
        try {
            SessionSpeakerInputDto input = SessionSpeakerInputDto.builder()
                    .startTime(startTime.strip())
                    .endTime(endTime.strip())
                    .build();
            return sessionService.updateSessionSpeakerLink(
                    UUID.fromString(sessionId), UUID.fromString(speakerId),
                    UUID.fromString(linkId), input);
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = "Remove a speaker from a session by their UUIDs.")
    public String deleteSpeakerFromSession(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker to remove") String speakerId
    ) {
        try {
            sessionService.deleteSpeakerFromSession(UUID.fromString(sessionId), UUID.fromString(speakerId));
            return String.format("Speaker %s removed from session %s.", speakerId, sessionId);
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = "Get time slots for a speaker in a session. Returns the link ID, start time, and end time for each slot.")
    public String getSessionSpeakerTimeSlots(
            @ToolParam(description = "UUID of the session") String sessionId,
            @ToolParam(description = "UUID of the speaker") String speakerId
    ) {
        try {
            var slots = sessionService.getSessionSpeakerTimeSlots(
                    UUID.fromString(sessionId), UUID.fromString(speakerId), "127.0.0.1");
            if (slots == null || slots.isEmpty()) {
                return String.format("No time slots found for speaker %s in session %s.", speakerId, sessionId);
            }
            var sb = new StringBuilder();
            sb.append("Time slots for speaker ").append(speakerId)
                    .append(" in session ").append(sessionId).append(":\n");
            for (var slot : slots) {
                sb.append("- Link ID: ").append(slot.getId())
                        .append(", Start: ").append(slot.getStartTime())
                        .append(", End: ").append(slot.getEndTime())
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    private static String formatSessionResponse(String header, SessionResponseDto session) {
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
                """, header, session.getId(), session.getTitle(),
                session.getRoom() != null ? session.getRoom().getName() : "N/A",
                session.getEvent() != null ? session.getEvent().getTitle() : "N/A",
                session.getStartDate(), session.getEndDate(),
                session.getCapacity(), session.isLive());
    }
}

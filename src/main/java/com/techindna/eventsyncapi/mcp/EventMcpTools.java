package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.dto.event.EventListResponseDto;
import com.techindna.eventsyncapi.service.EventService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.util.UUID;

import static com.techindna.eventsyncapi.mcp.McpToolSupport.logger;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.pageOrDefault;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.parseInstant;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.parseUuid;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.run;
import static com.techindna.eventsyncapi.mcp.McpToolSupport.sizeOrDefault;

@Component
@RequiredArgsConstructor
public class EventMcpTools {

    private static final String IP = "127.0.0.1";
    private static final Logger LOG = logger(EventMcpTools.class);

    private final EventService eventService;

    @Tool(name = "createEvent",
            description = """
                    Create a new event with the given details.

                    Returns a confirmation including id, title, location, and dates.

                    Validation rules — strict pass or call fails with 422:
                    • title: 1–50 chars, only a-z A-Z 0-9 - ' . and space. No commas,
                      slashes, or other punctuation; no leading/trailing spaces.
                    • description: non-blank, only A-Za-z0-9.,;"!'-
                    • startDate and endDate: required, endDate MUST be strictly after startDate.
                    • location: 1–50 chars, only a-z A-Z ' - and space. Must start and end with
                      a letter; no digits, no dots, no underscores.

                    Conflicts: duplicate title → 409 "Event '...' already exists.".

                    Example JSON for date fields (ISO-8601 instant):
                    ```json
                    {"startDate": "2026-07-15T09:00:00Z", "endDate": "2026-07-17T18:00:00Z"}
                    ```
                    """)
    public String createEvent(
            @ToolParam(description = "Title of the event (max 100 characters)") String title,
            @ToolParam(description = "Description of the event (max 1000 characters)") String description,
            @ToolParam(description = "Start date/time in ISO-8601 format, e.g. 2026-07-15T09:00:00Z") String startDate,
            @ToolParam(description = "End date/time in ISO-8601 format, e.g. 2026-07-17T18:00:00Z") String endDate,
            @ToolParam(description = "Location of the event (max 100 characters)") String location
    ) {
        return run(LOG, "createEvent", () -> {
            EventInputDto input = EventInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(parseInstant(startDate))
                    .endDate(parseInstant(endDate))
                    .location(location.strip())
                    .build();
            EventDetailResponseDto event = eventService.createEvent(input);
            return formatEventResponse("Event created:", event);
        });
    }

    @Tool(name = "listEvents",
            description = """
                    List events with optional filters and pagination.

                    Returns event titles, ids, locations, and live status, or 'No events found.' if empty.

                    Validation rules — strict pass or call fails with 422:
                    • title and location filters: each must contain only letters, digits,
                      spaces, hyphens, and apostrophes. Avoid commas, slashes, colons,
                      or other punctuation in the filter values.
                    """)
    public String listEvents(
            @ToolParam(description = "Optional search term to filter events by title") String title,
            @ToolParam(description = "Optional search term to filter events by location") String location,
            @ToolParam(description = "Page number (optional, defaults to 1)") Integer page,
            @ToolParam(description = "Items per page (optional, defaults to 10)") Integer size
    ) {
        return run(LOG, "listEvents", () -> {
            EventListResponseDto result = eventService.getAllEvents(
                    pageOrDefault(page), sizeOrDefault(size),
                    title, location, null, null, null, IP);
            if (result.getData() == null || result.getData().isEmpty()) {
                return "No events found.";
            }
            var sb = new StringBuilder();
            sb.append("Events (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var event : result.getData()) {
                sb.append("- ").append(event.getTitle())
                        .append(" (").append(event.getId()).append(")")
                        .append(" — ").append(event.getLocation())
                        .append(event.isLive() ? " [LIVE]" : "")
                        .append("\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "getEvent",
            description = """
                    Get details of a specific event by its UUID, including its sessions.

                    Returns the event details and its associated sessions (if any).
                    """)
    public String getEvent(
            @ToolParam(description = "UUID of the event") String id
    ) {
        return run(LOG, "getEvent", () -> {
            EventDetailResponseDto event = eventService.getEventById(parseUuid(id), IP);
            var sb = new StringBuilder(formatEventResponse("Event details:", event));
            if (event.getSessions() != null && !event.getSessions().isEmpty()) {
                sb.append("- Sessions (").append(event.getSessions().size()).append("):\n");
                for (var session : event.getSessions()) {
                    sb.append("  · ").append(session.getTitle())
                            .append(" (").append(session.getId()).append(")")
                            .append(session.isLive() ? " [LIVE]" : "")
                            .append("\n");
                }
            } else {
                sb.append("- Sessions: none\n");
            }
            return sb.toString();
        });
    }

    @Tool(name = "updateEvent",
            description = """
                    Update an existing event. All fields are required and will replace the current values.

                    Returns a confirmation including id, title, location, and dates.

                    Validation rules — strict pass or call fails with 422:
                    • title: 1–50 chars, only a-z A-Z 0-9 - ' . and space. No commas, slashes,
                      or other punctuation; no leading/trailing spaces.
                    • description: non-blank, only A-Za-z0-9.,;"!'-
                    • startDate and endDate: required, endDate MUST be strictly after startDate.
                    • location: 1–50 chars, only a-z A-Z ' - and space. Must start and end with
                      a letter; no digits, no dots, no underscores.

                    Conflicts: duplicate title → 409 "Event '...' already exists.".

                    Example JSON for date fields (ISO-8601 instant):
                    ```json
                    {"startDate": "2026-08-01T09:00:00Z", "endDate": "2026-08-03T18:00:00Z"}
                    ```
                    """)
    public String updateEvent(
            @ToolParam(description = "UUID of the event to update") String id,
            @ToolParam(description = "New title of the event (max 100 characters)") String title,
            @ToolParam(description = "New description of the event (max 1000 characters)") String description,
            @ToolParam(description = "New start date/time in ISO-8601 format, e.g. 2026-08-01T09:00:00Z") String startDate,
            @ToolParam(description = "New end date/time in ISO-8601 format, e.g. 2026-08-03T18:00:00Z") String endDate,
            @ToolParam(description = "New location of the event (max 100 characters)") String location
    ) {
        return run(LOG, "updateEvent", () -> {
            EventInputDto input = EventInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(parseInstant(startDate))
                    .endDate(parseInstant(endDate))
                    .location(location.strip())
                    .build();
            EventDetailResponseDto event = eventService.updateEvent(parseUuid(id), input);
            return formatEventResponse("Event updated:", event);
        });
    }

    @Tool(name = "deleteEvent",
            description = """
                    Delete an event by its UUID.

                    Returns a confirmation string.
                    """)
    public String deleteEvent(
            @ToolParam(description = "UUID of the event to delete") String id
    ) {
        return run(LOG, "deleteEvent", () -> {
            UUID eventId = parseUuid(id);
            eventService.deleteEvent(eventId);
            return String.format("Event %s deleted.", eventId);
        });
    }

    private static String formatEventResponse(String header, EventDetailResponseDto event) {
        return String.format("""
                        %s
                        - ID: %s
                        - Title: %s
                        - Location: %s
                        - Start: %s
                        - End: %s
                        - Live: %s
                        """,
                header, event.getId(), event.getTitle(),
                event.getLocation(), event.getStartDate(), event.getEndDate(),
                event.isLive());
    }
}

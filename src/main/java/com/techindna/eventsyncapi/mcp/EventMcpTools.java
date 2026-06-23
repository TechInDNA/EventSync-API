package com.techindna.eventsyncapi.mcp;

import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.dto.event.EventListResponseDto;
import com.techindna.eventsyncapi.exception.InternalServerErrorException;
import com.techindna.eventsyncapi.service.EventService;
import lombok.RequiredArgsConstructor;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class EventMcpTools {

    private final EventService eventService;

    @Tool(description = """
            Create a new event with the given details. Returns the created event id, title, and dates.
            
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
        try {
            EventInputDto input = EventInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(Instant.parse(startDate))
                    .endDate(Instant.parse(endDate))
                    .location(location.strip())
                    .build();
            EventDetailResponseDto event = eventService.createEvent(input);
            return String.format("""
                    Event created:
                    - ID: %s
                    - Title: %s
                    - Location: %s
                    - Start: %s
                    - End: %s
                    """, event.getId(), event.getTitle(), event.getLocation(), event.getStartDate(), event.getEndDate());
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = "List events with optional filters and pagination. Returns event names, IDs, locations, and live status.")
    public String listEvents(
            @ToolParam(description = "Optional search term to filter events by title") String title,
            @ToolParam(description = "Optional search term to filter events by location") String location,
            @ToolParam(description = "Page number (default: 1)") int page,
            @ToolParam(description = "Items per page (default: 10)") int size
    ) {
        try {
            EventListResponseDto result = eventService.getAllEvents(page, size, title, location,
                    null, null, null, "127.0.0.1");
            if (result.getData().isEmpty()) {
                return "No events found.";
            }
            StringBuilder sb = new StringBuilder();
            sb.append("Events (").append(result.getMeta().getTotal()).append(" total):\n");
            for (var event : result.getData()) {
                sb.append("- ").append(event.getTitle())
                        .append(" (").append(event.getId()).append(")")
                        .append(" — ").append(event.getLocation())
                        .append(event.isLive() ? " [LIVE]" : "")
                        .append("\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = "Get details of a specific event by its UUID, including its sessions.")
    public String getEvent(
            @ToolParam(description = "UUID of the event") String id
    ) {
        try {
            EventDetailResponseDto event = eventService.getEventById(UUID.fromString(id), "127.0.0.1");
            StringBuilder sb = new StringBuilder();
            sb.append("Event details:\n")
                    .append("- ID: ").append(event.getId()).append("\n")
                    .append("- Title: ").append(event.getTitle()).append("\n")
                    .append("- Description: ").append(event.getDescription()).append("\n")
                    .append("- Location: ").append(event.getLocation()).append("\n")
                    .append("- Start: ").append(event.getStartDate()).append("\n")
                    .append("- End: ").append(event.getEndDate()).append("\n")
                    .append("- Created at: ").append(event.getCreatedAt()).append("\n")
                    .append("- Live: ").append(event.isLive()).append("\n");
            if (event.getSessions() != null && !event.getSessions().isEmpty()) {
                sb.append("- Sessions (").append(event.getSessions().size()).append("):\n");
                for (var session : event.getSessions()) {
                    sb.append("  • ").append(session.getTitle())
                            .append(" (").append(session.getId()).append(")")
                            .append(session.isLive() ? " [LIVE]" : "")
                            .append("\n");
                }
            } else {
                sb.append("- Sessions: none\n");
            }
            return sb.toString();
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = """
            Update an existing event. All fields are required and will replace the current values.
            
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
        try {
            EventInputDto input = EventInputDto.builder()
                    .title(title.strip())
                    .description(description.strip())
                    .startDate(Instant.parse(startDate))
                    .endDate(Instant.parse(endDate))
                    .location(location.strip())
                    .build();
            EventDetailResponseDto event = eventService.updateEvent(UUID.fromString(id), input);
            return String.format(
                    """
                    Event updated:
                    - ID: %s
                    - Title: %s
                    - Location: %s
                    - Start: %s
                    - End: %s
                    """, event.getId(), event.getTitle(), event.getLocation(), event.getStartDate(), event.getEndDate());
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    @Tool(description = "Delete an event by its UUID.")
    public String deleteEvent(
            @ToolParam(description = "UUID of the event to delete") String id
    ) {
        try {
            eventService.deleteEvent(UUID.fromString(id));
            return "Event " + id + " deleted.";
        } catch (Exception e) {
            return String.format("Operation failed: %s", e.getMessage());
        }
    }
}

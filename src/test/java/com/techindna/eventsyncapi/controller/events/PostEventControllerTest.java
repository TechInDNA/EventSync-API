package com.techindna.eventsyncapi.controller.events;

import com.techindna.eventsyncapi.controller.EventController;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.dto.event.EventResponseDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PostEventControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant NOW = Instant.now();

    PostEventControllerTest() {
        eventService = mock(EventService.class);
        objectMapper = new ObjectMapper();
        var controller = new EventController(eventService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("POST /events with valid body returns 201 and created event")
    void createEvent_withValidBody_returns201() throws Exception {
        var request = EventInputDto.builder()
                .title("Annual Tech Conference")
                .description("A great tech conference")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                .location("Antananarivo")
                .build();

        var response = EventResponseDto.builder()
                .id(EVENT_ID)
                .title("Annual Tech Conference")
                .description("A great tech conference")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                .location("Antananarivo")
                .createdAt(NOW)
                .live(false)
                .build();

        when(eventService.createEvent(any(EventInputDto.class))).thenReturn(response);

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.title").value("Annual Tech Conference"))
                .andExpect(jsonPath("$.description").value("A great tech conference"))
                .andExpect(jsonPath("$.startDate").value("2025-06-01T09:00:00Z"))
                .andExpect(jsonPath("$.endDate").value("2025-06-03T18:00:00Z"))
                .andExpect(jsonPath("$.location").value("Antananarivo"))
                .andExpect(jsonPath("$.isLive").value(false));
    }

    @Test
    @DisplayName("POST /events with empty title returns 422")
    void createEvent_withEmptyTitle_returns422() throws Exception {
        var request = EventInputDto.builder()
                .title("")
                .description("A great tech conference")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                .location("Antananarivo")
                .build();

        when(eventService.createEvent(any(EventInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field title is required and cannot be blank."));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("POST /events with duplicate title returns 409")
    void createEvent_withDuplicateTitle_returns409() throws Exception {
        var request = EventInputDto.builder()
                .title("Annual Tech Conference")
                .description("A great tech conference")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                .location("Antananarivo")
                .build();

        when(eventService.createEvent(any(EventInputDto.class)))
                .thenThrow(new ConflictException("Event 'Annual Tech Conference' already exists."));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Event 'Annual Tech Conference' already exists."));
    }

    @Test
    @DisplayName("POST /events with endDate before startDate returns 422")
    void createEvent_withEndDateBeforeStartDate_returns422() throws Exception {
        var request = EventInputDto.builder()
                .title("Annual Tech Conference")
                .description("A great tech conference")
                .startDate(Instant.parse("2025-06-03T18:00:00Z"))
                .endDate(Instant.parse("2025-06-01T09:00:00Z"))
                .location("Antananarivo")
                .build();

        when(eventService.createEvent(any(EventInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field endDate must be after startDate."));

        mockMvc.perform(post("/events")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("The field endDate must be after startDate."));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

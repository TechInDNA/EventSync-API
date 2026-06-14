package com.techindna.eventsyncapi.controller.events;

import com.techindna.eventsyncapi.controller.EventController;
import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.Mockito.eq;

class PutEventControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final Instant START_DATE = Instant.parse("2025-06-01T09:00:00Z");
    private static final Instant END_DATE = Instant.parse("2025-06-03T18:00:00Z");

    PutEventControllerTest() {
        eventService = mock(EventService.class);
        objectMapper = new ObjectMapper();
        var controller = new EventController(eventService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("PUT /events/{id} with valid body returns 200 and updated event")
    void updateEvent_withValidData_returns200() throws Exception {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        var response = EventDetailResponseDto.builder()
                .id(EVENT_ID)
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .live(false)
                .build();

        when(eventService.updateEvent(eq(EVENT_ID), any(EventInputDto.class))).thenReturn(response);

        mockMvc.perform(put("/events/{id}", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.title").value("Updated Conference"))
                .andExpect(jsonPath("$.description").value("An updated description"))
                .andExpect(jsonPath("$.location").value("Paris"))
                .andExpect(jsonPath("$.isLive").value(false))
                .andExpect(jsonPath("$.sessions").value((Object) null));
    }

    @Test
    @DisplayName("PUT /events/{id} with empty title returns 422")
    void updateEvent_withEmptyTitle_returns422() throws Exception {
        var request = EventInputDto.builder()
                .title("")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        when(eventService.updateEvent(eq(EVENT_ID), any(EventInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field title is required and cannot be blank."));

        mockMvc.perform(put("/events/{id}", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("PUT /events/{id} with unknown id returns 404")
    void updateEvent_withUnknownId_returns404() throws Exception {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        when(eventService.updateEvent(eq(EVENT_ID), any(EventInputDto.class)))
                .thenThrow(new NotFoundException("Event " + EVENT_ID + " not found."));

        mockMvc.perform(put("/events/{id}", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Event " + EVENT_ID + " not found."));
    }

    @Test
    @DisplayName("PUT /events/{id} with duplicate title returns 409")
    void updateEvent_withDuplicateTitle_returns409() throws Exception {
        var request = EventInputDto.builder()
                .title("Existing Event")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        when(eventService.updateEvent(eq(EVENT_ID), any(EventInputDto.class)))
                .thenThrow(new ConflictException("Event 'Existing Event' already exists."));

        mockMvc.perform(put("/events/{id}", EVENT_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Event 'Existing Event' already exists."));
    }

    @Test
    @DisplayName("PUT /events/{id} with endDate before startDate returns 422")
    void updateEvent_withEndDateBeforeStartDate_returns422() throws Exception {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(Instant.parse("2025-06-03T18:00:00Z"))
                .endDate(Instant.parse("2025-06-01T09:00:00Z"))
                .location("Paris")
                .build();

        when(eventService.updateEvent(eq(EVENT_ID), any(EventInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field endDate must be after startDate."));

        mockMvc.perform(put("/events/{id}", EVENT_ID)
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

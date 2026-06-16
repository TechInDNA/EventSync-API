package com.techindna.eventsyncapi.controller.sessions;

import com.techindna.eventsyncapi.controller.SessionController;
import com.techindna.eventsyncapi.dto.session.EventRefDto;
import com.techindna.eventsyncapi.dto.session.RoomRefDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.time.Instant;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PutSessionControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID EVENT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");

    PutSessionControllerTest() {
        sessionService = mock(SessionService.class);
        objectMapper = new ObjectMapper();
        var controller = new SessionController(sessionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("PUT /sessions/{id} with valid body returns 200 and updated session")
    void updateSession_withValidInput_returns200() throws Exception {
        var request = SessionUpdateInputDto.builder()
                .title("Updated Session")
                .description("An updated description")
                .startDate(Instant.parse("2025-07-01T09:00:00Z"))
                .endDate(Instant.parse("2025-07-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(200)
                .eventId(EVENT_ID)
                .build();

        var response = SessionResponseDto.builder()
                .id(SESSION_ID)
                .title("Updated Session")
                .description("An updated description")
                .startDate(Instant.parse("2025-07-01T09:00:00Z"))
                .endDate(Instant.parse("2025-07-01T10:00:00Z"))
                .room(RoomRefDto.builder().id(ROOM_ID).name("Conference Hall").build())
                .capacity(200)
                .event(EventRefDto.builder().id(EVENT_ID).title("TechConf").build())
                .speakers(Collections.emptyList())
                .isLive(false)
                .build();

        when(sessionService.updateSession(eq(SESSION_ID), any(SessionUpdateInputDto.class))).thenReturn(response);

        mockMvc.perform(put("/sessions/{id}", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.title").value("Updated Session"))
                .andExpect(jsonPath("$.description").value("An updated description"))
                .andExpect(jsonPath("$.capacity").value(200))
                .andExpect(jsonPath("$.room.id").value(ROOM_ID.toString()))
                .andExpect(jsonPath("$.room.name").value("Conference Hall"))
                .andExpect(jsonPath("$.event.id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.event.title").value("TechConf"))
                .andExpect(jsonPath("$.isLive").value(false))
                .andExpect(jsonPath("$.speakers").isArray())
                .andExpect(jsonPath("$.speakers").isEmpty());
    }

    @Test
    @DisplayName("PUT /sessions/{id} with invalid data returns 422")
    void updateSession_withInvalidData_returns422() throws Exception {
        var request = SessionUpdateInputDto.builder()
                .title("")
                .description("Desc")
                .startDate(Instant.parse("2025-07-01T09:00:00Z"))
                .endDate(Instant.parse("2025-07-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(200)
                .eventId(EVENT_ID)
                .build();

        when(sessionService.updateSession(eq(SESSION_ID), any(SessionUpdateInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field title is required and cannot be blank."));

        mockMvc.perform(put("/sessions/{id}", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("PUT /sessions/{id} with unknown id returns 404")
    void updateSession_withUnknownId_returns404() throws Exception {
        var request = SessionUpdateInputDto.builder()
                .title("Updated Session")
                .description("Desc")
                .startDate(Instant.parse("2025-07-01T09:00:00Z"))
                .endDate(Instant.parse("2025-07-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(200)
                .eventId(EVENT_ID)
                .build();

        when(sessionService.updateSession(eq(SESSION_ID), any(SessionUpdateInputDto.class)))
                .thenThrow(new NotFoundException("Session " + SESSION_ID + " not found."));

        mockMvc.perform(put("/sessions/{id}", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Session " + SESSION_ID + " not found."));
    }

    @Test
    @DisplayName("PUT /sessions/{id} with duplicate title returns 409")
    void updateSession_withDuplicateTitle_returns409() throws Exception {
        var request = SessionUpdateInputDto.builder()
                .title("Existing Title")
                .description("Desc")
                .startDate(Instant.parse("2025-07-01T09:00:00Z"))
                .endDate(Instant.parse("2025-07-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(200)
                .eventId(EVENT_ID)
                .build();

        when(sessionService.updateSession(eq(SESSION_ID), any(SessionUpdateInputDto.class)))
                .thenThrow(new ConflictException("Session 'Existing Title' already exists."));

        mockMvc.perform(put("/sessions/{id}", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Session 'Existing Title' already exists."));
    }

    @Test
    @DisplayName("PUT /sessions/{id} with non-existent room or event returns 404")
    void updateSession_withNonExistentRoomOrEvent_returns404() throws Exception {
        var request = SessionUpdateInputDto.builder()
                .title("Updated Session")
                .description("Desc")
                .startDate(Instant.parse("2025-07-01T09:00:00Z"))
                .endDate(Instant.parse("2025-07-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(200)
                .eventId(EVENT_ID)
                .build();

        when(sessionService.updateSession(eq(SESSION_ID), any(SessionUpdateInputDto.class)))
                .thenThrow(new NotFoundException(
                        String.format("Room (%s) or event (%s) not found.", ROOM_ID, EVENT_ID)
                ));

        mockMvc.perform(put("/sessions/{id}", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        String.format("Room (%s) or event (%s) not found.", ROOM_ID, EVENT_ID)
                ));
    }

    @Test
    @DisplayName("PUT /sessions/{id} with endDate before startDate returns 422")
    void updateSession_withEndDateBeforeStartDate_returns422() throws Exception {
        var request = SessionUpdateInputDto.builder()
                .title("Valid Title")
                .description("Desc")
                .startDate(Instant.parse("2025-07-01T10:00:00Z"))
                .endDate(Instant.parse("2025-07-01T09:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(200)
                .eventId(EVENT_ID)
                .build();

        when(sessionService.updateSession(eq(SESSION_ID), any(SessionUpdateInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field endDate must be after startDate."));

        mockMvc.perform(put("/sessions/{id}", SESSION_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

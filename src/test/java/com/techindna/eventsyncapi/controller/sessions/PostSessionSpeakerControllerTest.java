package com.techindna.eventsyncapi.controller.sessions;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.techindna.eventsyncapi.controller.SessionController;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PostSessionSpeakerControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEAKER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    PostSessionSpeakerControllerTest() {
        sessionService = mock(SessionService.class);
        objectMapper = new ObjectMapper();
        var controller = new SessionController(sessionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("POST /sessions/{sessionId}/speaker/{speakerId} with valid input returns 201")
    void addSpeakerToSession_withValidInput_returns201() throws Exception {
        var request = SessionSpeakerInputDto.builder()
                .startTime("10:00:00+03:00")
                .endTime("11:30:00+03:00")
                .build();

        when(sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request))
                .thenReturn("Speaker linked to session.");

        mockMvc.perform(post("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(content().string("Speaker linked to session."));
    }

    @Test
    @DisplayName("POST /sessions/{sessionId}/speaker/{speakerId} with non-existent session or speaker returns 404")
    void addSpeakerToSession_withNonExistentResources_returns404() throws Exception {
        var request = SessionSpeakerInputDto.builder()
                .startTime("10:00:00+03:00")
                .endTime("11:30:00+03:00")
                .build();

        when(sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request))
                .thenThrow(new NotFoundException(
                        String.format("Session (%s) or speaker (%s) not found.", SESSION_ID, SPEAKER_ID)
                ));

        mockMvc.perform(post("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value(
                        String.format("Session (%s) or speaker (%s) not found.", SESSION_ID, SPEAKER_ID)
                ));
    }

    @Test
    @DisplayName("POST /sessions/{sessionId}/speaker/{speakerId} with null startTime returns 422")
    void addSpeakerToSession_withNullStartTime_returns422() throws Exception {
        var request = SessionSpeakerInputDto.builder()
                .startTime(null)
                .endTime("11:30:00+03:00")
                .build();

        when(sessionService.addSpeakerToSession(any(), any(), any()))
                .thenThrow(new UnprocessableEntityException("The field startTime is required."));

        mockMvc.perform(post("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("POST /sessions/{sessionId}/speaker/{speakerId} with endTime before startTime returns 422")
    void addSpeakerToSession_withEndTimeBeforeStartTime_returns422() throws Exception {
        var request = SessionSpeakerInputDto.builder()
                .startTime("14:00:00+03:00")
                .endTime("10:00:00+03:00")
                .build();

        when(sessionService.addSpeakerToSession(any(), any(), any()))
                .thenThrow(new UnprocessableEntityException("The field endTime must be after startTime."));

        mockMvc.perform(post("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("POST /sessions/{sessionId}/speaker/{speakerId} with invalid time format returns 422")
    void addSpeakerToSession_withInvalidTimeFormat_returns422() throws Exception {
        var request = SessionSpeakerInputDto.builder()
                .startTime("invalid")
                .endTime("11:30:00+03:00")
                .build();

        when(sessionService.addSpeakerToSession(any(), any(), any()))
                .thenThrow(new UnprocessableEntityException(
                        "Invalid format for startTime: expected ISO timestamp with timezone (yyyy-MM-ddTHH:mm:ss±HH:mm)."
                ));

        mockMvc.perform(post("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("POST /sessions/{sessionId}/speaker/{speakerId} with busy room returns 409")
    void addSpeakerToSession_withBusyRoom_returns409() throws Exception {
        var request = SessionSpeakerInputDto.builder()
                .startTime("10:00:00+03:00")
                .endTime("11:30:00+03:00")
                .build();

        when(sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request))
                .thenThrow(new ConflictException("The room is already occupied during the requested time slot."));

        mockMvc.perform(post("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("The room is already occupied during the requested time slot."));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.techindna.eventsyncapi.controller.sessions;

import com.techindna.eventsyncapi.controller.SessionController;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerTimeSlotDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetSessionSpeakerControllerTest {

    private final MockMvc mockMvc;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEAKER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    GetSessionSpeakerControllerTest() {
        sessionService = mock(SessionService.class);
        var controller = new SessionController(sessionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /sessions/{sessionId}/speaker/{speakerId} with existing link returns 200 and time slots")
    void getSessionSpeakerTimeSlots_withExistingLink_returns200() throws Exception {
        var timeSlots = List.of(
                SessionSpeakerTimeSlotDto.builder()
                        .startTime("2026-08-02 14:00:00+03")
                        .endTime("2026-08-02 15:30:00+03")
                        .build()
        );
        when(sessionService.getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, "127.0.0.1")).thenReturn(timeSlots);

        mockMvc.perform(get("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1))
                .andExpect(jsonPath("$[0].startTime").value("2026-08-02 14:00:00+03"))
                .andExpect(jsonPath("$[0].endTime").value("2026-08-02 15:30:00+03"));

        verify(sessionService).getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, "127.0.0.1");
    }

    @Test
    @DisplayName("GET /sessions/{sessionId}/speaker/{speakerId} with multiple time slots returns 200 and all slots")
    void getSessionSpeakerTimeSlots_withMultipleSlots_returns200() throws Exception {
        var timeSlots = List.of(
                SessionSpeakerTimeSlotDto.builder()
                        .startTime("2026-08-02 10:00:00+03")
                        .endTime("2026-08-02 11:00:00+03")
                        .build(),
                SessionSpeakerTimeSlotDto.builder()
                        .startTime("2026-08-02 14:00:00+03")
                        .endTime("2026-08-02 15:30:00+03")
                        .build()
        );
        when(sessionService.getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, "127.0.0.1")).thenReturn(timeSlots);

        mockMvc.perform(get("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].startTime").value("2026-08-02 10:00:00+03"))
                .andExpect(jsonPath("$[1].startTime").value("2026-08-02 14:00:00+03"));
    }

    @Test
    @DisplayName("GET /sessions/{sessionId}/speaker/{speakerId} with non-existent link returns 404")
    void getSessionSpeakerTimeSlots_withNonExistentLink_returns404() throws Exception {
        when(sessionService.getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, "127.0.0.1"))
                .thenThrow(new NotFoundException("Speaker is not linked to session."));

        mockMvc.perform(get("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID))
                .andExpect(status().isNotFound());

        verify(sessionService).getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, "127.0.0.1");
    }
}

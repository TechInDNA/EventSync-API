package com.techindna.eventsyncapi.controller.sessions;

import com.techindna.eventsyncapi.controller.SessionController;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class DeleteSessionSpeakerControllerTest {

    private final MockMvc mockMvc;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEAKER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    DeleteSessionSpeakerControllerTest() {
        sessionService = mock(SessionService.class);
        var controller = new SessionController(sessionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("DELETE /sessions/{sessionId}/speaker/{speakerId} with existing link returns 204")
    void deleteSpeakerFromSession_withExistingLink_returns204() throws Exception {
        doNothing().when(sessionService).deleteSpeakerFromSession(SESSION_ID, SPEAKER_ID);

        mockMvc.perform(delete("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID))
                .andExpect(status().isNoContent());

        verify(sessionService).deleteSpeakerFromSession(SESSION_ID, SPEAKER_ID);
    }

    @Test
    @DisplayName("DELETE /sessions/{sessionId}/speaker/{speakerId} with non-existent link returns 404")
    void deleteSpeakerFromSession_withNonExistentLink_returns404() throws Exception {
        doThrow(new NotFoundException(
                String.format("Session (%s) or speaker (%s) not found.", SESSION_ID, SPEAKER_ID)
        )).when(sessionService).deleteSpeakerFromSession(SESSION_ID, SPEAKER_ID);

        mockMvc.perform(delete("/sessions/{sessionId}/speaker/{speakerId}", SESSION_ID, SPEAKER_ID))
                .andExpect(status().isNotFound());

        verify(sessionService).deleteSpeakerFromSession(SESSION_ID, SPEAKER_ID);
    }
}

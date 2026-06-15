package com.techindna.eventsyncapi.controller.speakers;

import com.techindna.eventsyncapi.controller.SpeakerController;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.SpeakerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class DeleteSpeakerControllerTest {

    private final MockMvc mockMvc;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    DeleteSpeakerControllerTest() {
        speakerService = mock(SpeakerService.class);
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("DELETE /speakers/{id} with valid id returns 204")
    void deleteSpeaker_withValidId_returns204() throws Exception {
        doNothing().when(speakerService).deleteSpeaker(SPEAKER_ID);

        mockMvc.perform(delete("/speakers/{id}", SPEAKER_ID))
                .andExpect(status().isNoContent());

        verify(speakerService).deleteSpeaker(SPEAKER_ID);
    }

    @Test
    @DisplayName("DELETE /speakers/{id} with unknown id returns 404")
    void deleteSpeaker_withUnknownId_returns404() throws Exception {
        doThrow(new NotFoundException("Speaker " + SPEAKER_ID + " not found."))
                .when(speakerService).deleteSpeaker(SPEAKER_ID);

        mockMvc.perform(delete("/speakers/{id}", SPEAKER_ID))
                .andExpect(status().isNotFound());

        verify(speakerService).deleteSpeaker(SPEAKER_ID);
    }
}

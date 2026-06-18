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

class DeleteSpeakerExternalLinkControllerTest {

    private final MockMvc mockMvc;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID EXTERNAL_LINK_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    DeleteSpeakerExternalLinkControllerTest() {
        speakerService = mock(SpeakerService.class);
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("DELETE /speakers/{id}/external-link with valid ids returns 204")
    void deleteExternalLink_withValidIds_returns204() throws Exception {
        doNothing().when(speakerService).deleteExternalLink(SPEAKER_ID, EXTERNAL_LINK_ID);

        mockMvc.perform(delete("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("externalLinkId", EXTERNAL_LINK_ID.toString()))
                .andExpect(status().isNoContent());

        verify(speakerService).deleteExternalLink(SPEAKER_ID, EXTERNAL_LINK_ID);
    }

    @Test
    @DisplayName("DELETE /speakers/{id}/external-link with unknown speaker returns 404")
    void deleteExternalLink_withUnknownSpeaker_returns404() throws Exception {
        doThrow(new NotFoundException("Speaker " + SPEAKER_ID + " not found."))
                .when(speakerService).deleteExternalLink(SPEAKER_ID, EXTERNAL_LINK_ID);

        mockMvc.perform(delete("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("externalLinkId", EXTERNAL_LINK_ID.toString()))
                .andExpect(status().isNotFound());

        verify(speakerService).deleteExternalLink(SPEAKER_ID, EXTERNAL_LINK_ID);
    }

    @Test
    @DisplayName("DELETE /speakers/{id}/external-link with unknown external link returns 404")
    void deleteExternalLink_withUnknownExternalLink_returns404() throws Exception {
        doThrow(new NotFoundException(
                "External link " + EXTERNAL_LINK_ID + " not found for speaker " + SPEAKER_ID + "."))
                .when(speakerService).deleteExternalLink(SPEAKER_ID, EXTERNAL_LINK_ID);

        mockMvc.perform(delete("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("externalLinkId", EXTERNAL_LINK_ID.toString()))
                .andExpect(status().isNotFound());

        verify(speakerService).deleteExternalLink(SPEAKER_ID, EXTERNAL_LINK_ID);
    }
}

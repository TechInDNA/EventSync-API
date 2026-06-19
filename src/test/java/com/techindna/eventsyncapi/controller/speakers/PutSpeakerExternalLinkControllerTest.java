package com.techindna.eventsyncapi.controller.speakers;

import com.techindna.eventsyncapi.controller.SpeakerController;
import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.SpeakerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PutSpeakerExternalLinkControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String URL_NAME = "Twitter";

    PutSpeakerExternalLinkControllerTest() {
        speakerService = mock(SpeakerService.class);
        objectMapper = new ObjectMapper();
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("PUT /speakers/{id}/external-link with valid body returns 200 and updated links")
    void updateExternalLink_withValidInput_returns200() throws Exception {
        var request = new ExternalLinkDto("Twitter", "https://twitter.com/newhandle");
        var response = List.of(
                new ExternalLinkDto("Twitter", "https://twitter.com/newhandle"),
                new ExternalLinkDto("GitHub", "https://github.com/john")
        );

        when(speakerService.updateExternalLink(eq(SPEAKER_ID), eq(URL_NAME), any(ExternalLinkDto.class)))
                .thenReturn(response);

        mockMvc.perform(put("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("urlName", URL_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].name").value("Twitter"))
                .andExpect(jsonPath("$[0].url").value("https://twitter.com/newhandle"))
                .andExpect(jsonPath("$[1].name").value("GitHub"));
    }

    @Test
    @DisplayName("PUT /speakers/{id}/external-link with unknown speaker returns 404")
    void updateExternalLink_withUnknownSpeaker_returns404() throws Exception {
        var request = new ExternalLinkDto("Twitter", "https://twitter.com/newhandle");

        when(speakerService.updateExternalLink(eq(SPEAKER_ID), eq(URL_NAME), any(ExternalLinkDto.class)))
                .thenThrow(new NotFoundException(
                        String.format("Speaker %s not found.", SPEAKER_ID)));

        mockMvc.perform(put("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("urlName", URL_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /speakers/{id}/external-link with unknown urlName returns 404")
    void updateExternalLink_withUnknownUrlName_returns404() throws Exception {
        var request = new ExternalLinkDto("UnknownLink", "https://example.com");

        when(speakerService.updateExternalLink(eq(SPEAKER_ID), eq("UnknownLink"), any(ExternalLinkDto.class)))
                .thenThrow(new NotFoundException(
                        String.format("Speaker %s or external link 'UnknownLink' not found.", SPEAKER_ID)));

        mockMvc.perform(put("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("urlName", "UnknownLink")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"));
    }

    @Test
    @DisplayName("PUT /speakers/{id}/external-link with duplicate url returns 409")
    void updateExternalLink_withDuplicateUrl_returns409() throws Exception {
        var request = new ExternalLinkDto("Twitter", "https://github.com/existing");

        when(speakerService.updateExternalLink(eq(SPEAKER_ID), eq(URL_NAME), any(ExternalLinkDto.class)))
                .thenThrow(new ConflictException("URL https://github.com/existing already exists."));

        mockMvc.perform(put("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("urlName", URL_NAME)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"));
    }

    @Test
    @DisplayName("PUT /speakers/{id}/external-link with invalid body returns 422")
    void updateExternalLink_withInvalidBody_returns422() throws Exception {
        var request = new ExternalLinkDto("", "");

        when(speakerService.updateExternalLink(eq(SPEAKER_ID), eq(URL_NAME), any(ExternalLinkDto.class)))
                .thenThrow(new UnprocessableEntityException("The field name is required and cannot be blank."));

        mockMvc.perform(put("/speakers/{id}/external-link", SPEAKER_ID)
                        .param("urlName", URL_NAME)
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

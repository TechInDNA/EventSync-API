package com.techindna.eventsyncapi.controller.speakers;

import com.techindna.eventsyncapi.controller.SpeakerController;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.SpeakerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.Mockito.eq;

class PutSpeakerControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PutSpeakerControllerTest() {
        speakerService = mock(SpeakerService.class);
        objectMapper = new ObjectMapper();
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("PUT /speakers/{id} with valid body returns 200 and updated speaker")
    void updateSpeaker_withValidData_returns200() throws Exception {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.updated@example.com")
                .build();

        var response = SpeakerUpdateResponseDto.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john.updated@example.com")
                .build();

        when(speakerService.updateSpeaker(eq(SPEAKER_ID), any(SpeakerUpdateInputDto.class))).thenReturn(response);

        mockMvc.perform(put("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SPEAKER_ID.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.email").value("john.updated@example.com"));
    }

    @Test
    @DisplayName("PUT /speakers/{id} with unknown id returns 404")
    void updateSpeaker_withUnknownId_returns404() throws Exception {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(speakerService.updateSpeaker(eq(SPEAKER_ID), any(SpeakerUpdateInputDto.class)))
                .thenThrow(new NotFoundException("Speaker " + SPEAKER_ID + " not found."));

        mockMvc.perform(put("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Speaker " + SPEAKER_ID + " not found."));
    }

    @Test
    @DisplayName("PUT /speakers/{id} with empty firstName returns 422")
    void updateSpeaker_withEmptyFirstName_returns422() throws Exception {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(speakerService.updateSpeaker(eq(SPEAKER_ID), any(SpeakerUpdateInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field firstName is required and cannot be blank."));

        mockMvc.perform(put("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("The field firstName is required and cannot be blank."));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

package com.techindna.eventsyncapi.controller.speakers;

import com.techindna.eventsyncapi.controller.SpeakerController;
import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SessionForSpeakerDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.SpeakerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetSpeakerByIdControllerTest {

    private final MockMvc mockMvc;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    GetSpeakerByIdControllerTest() {
        speakerService = mock(SpeakerService.class);
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /speakers/{id} with valid id returns 200 and speaker details")
    void getSpeakerById_withValidId_returns200() throws Exception {
        var response = SpeakerDetailResponseDto.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .profilePicture("https://example.com/avatar.jpg")
                .bio("Experienced speaker")
                .externalLinks(List.of(
                        new ExternalLinkDto("Twitter", "https://twitter.com/john")
                ))
                .build();

        when(speakerService.getSpeakerById(SPEAKER_ID)).thenReturn(response);

        mockMvc.perform(get("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(SPEAKER_ID.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"))
                .andExpect(jsonPath("$.profilePicture").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.bio").value("Experienced speaker"))
                .andExpect(jsonPath("$.externalLinks[0].name").value("Twitter"))
                .andExpect(jsonPath("$.externalLinks[0].url").value("https://twitter.com/john"));
    }

    @Test
    @DisplayName("GET /speakers/{id} with sessions returns 200 and sessions array")
    void getSpeakerById_withSessions_returns200WithSessions() throws Exception {
        var session = SessionForSpeakerDto.builder()
                .id(UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901"))
                .title("Keynote")
                .build();

        var response = SpeakerDetailResponseDto.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .sessions(List.of(session))
                .build();

        when(speakerService.getSpeakerById(SPEAKER_ID)).thenReturn(response);

        mockMvc.perform(get("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.sessions[0].id").value("b2c3d4e5-f6a7-8901-bcde-f12345678901"))
                .andExpect(jsonPath("$.sessions[0].title").value("Keynote"));
    }

    @Test
    @DisplayName("GET /speakers/{id} with unknown id returns 404")
    void getSpeakerById_withUnknownId_returns404() throws Exception {
        when(speakerService.getSpeakerById(SPEAKER_ID))
                .thenThrow(new NotFoundException("Speaker " + SPEAKER_ID + " not found."));

        mockMvc.perform(get("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Speaker " + SPEAKER_ID + " not found."));
    }

    @Test
    @DisplayName("GET /speakers/{id} with null externalLinks returns 200 without externalLinks")
    void getSpeakerById_withNullExternalLinks_returns200() throws Exception {
        var response = SpeakerDetailResponseDto.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(speakerService.getSpeakerById(SPEAKER_ID)).thenReturn(response);

        mockMvc.perform(get("/speakers/{id}", SPEAKER_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.externalLinks").doesNotExist())
                .andExpect(jsonPath("$.sessions").doesNotExist());
    }
}

package com.techindna.eventsyncapi.controller.speakers;

import com.techindna.eventsyncapi.controller.SpeakerController;
import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
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
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PostSpeakerControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PostSpeakerControllerTest() {
        speakerService = mock(SpeakerService.class);
        objectMapper = new ObjectMapper();
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("POST /speakers with valid body returns 201 and created speaker")
    void createSpeaker_withValidInput_returns201() throws Exception {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        var response = SpeakerResponseDto.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(speakerService.createSpeaker(any(SpeakerInputDto.class))).thenReturn(response);

        mockMvc.perform(post("/speakers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SPEAKER_ID.toString()))
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Doe"));
    }

    @Test
    @DisplayName("POST /speakers with empty firstName returns 422")
    void createSpeaker_withEmptyFirstName_returns422() throws Exception {
        var request = SpeakerInputDto.builder()
                .firstName("")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        when(speakerService.createSpeaker(any(SpeakerInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field firstName is required and cannot be blank."));

        mockMvc.perform(post("/speakers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"))
                .andExpect(jsonPath("$.message").value("The field firstName is required and cannot be blank."));
    }

    @Test
    @DisplayName("POST /speakers with duplicate email returns 409")
    void createSpeaker_withDuplicateEmail_returns409() throws Exception {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com")
                .build();

        when(speakerService.createSpeaker(any(SpeakerInputDto.class)))
                .thenThrow(new ConflictException("Email existing@example.com already exists."));

        mockMvc.perform(post("/speakers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.status").value(409))
                .andExpect(jsonPath("$.error").value("Conflict"))
                .andExpect(jsonPath("$.message").value("Email existing@example.com already exists."));
    }

    @Test
    @DisplayName("POST /speakers with invalid email format returns 422")
    void createSpeaker_withInvalidEmail_returns422() throws Exception {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("not-an-email")
                .build();

        when(speakerService.createSpeaker(any(SpeakerInputDto.class)))
                .thenThrow(new UnprocessableEntityException("Invalid email format: 'not-an-email'"));

        mockMvc.perform(post("/speakers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422));
    }

    @Test
    @DisplayName("POST /speakers with external links returns 201 with links")
    void createSpeaker_withExternalLinks_returns201WithLinks() throws Exception {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .externalLinks(List.of(
                        new ExternalLinkDto("Twitter", "https://twitter.com/john"),
                        new ExternalLinkDto("GitHub", "https://github.com/john")
                ))
                .build();

        var response = SpeakerResponseDto.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .externalLinks(List.of(
                        new ExternalLinkDto("Twitter", "https://twitter.com/john"),
                        new ExternalLinkDto("GitHub", "https://github.com/john")
                ))
                .build();

        when(speakerService.createSpeaker(any(SpeakerInputDto.class))).thenReturn(response);

        mockMvc.perform(post("/speakers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(SPEAKER_ID.toString()))
                .andExpect(jsonPath("$.externalLinks[0].name").value("Twitter"))
                .andExpect(jsonPath("$.externalLinks[1].url").value("https://github.com/john"));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

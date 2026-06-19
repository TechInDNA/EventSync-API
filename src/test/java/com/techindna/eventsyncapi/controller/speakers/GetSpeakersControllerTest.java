package com.techindna.eventsyncapi.controller.speakers;

import com.techindna.eventsyncapi.controller.SpeakerController;
import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerListResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.service.SpeakerService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetSpeakersControllerTest {

    private final MockMvc mockMvc;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    GetSpeakersControllerTest() {
        speakerService = mock(SpeakerService.class);
        var controller = new SpeakerController(speakerService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /speakers returns 200 with paginated speaker list")
    void getAllSpeakers_withDefaultPagination_returns200AndList() throws Exception {
        var speakers = List.of(
                SpeakerResponseDto.builder()
                        .id(SPEAKER_ID)
                        .firstName("John")
                        .lastName("Doe")
                        .profilePicture("https://example.com/avatar.jpg")
                        .bio("Experienced speaker")
                        .externalLinks(List.of(
                                new ExternalLinkDto("Twitter", "https://twitter.com/john")
                        ))
                        .build()
        );
        var response = SpeakerListResponseDto.builder()
                .data(speakers)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(speakerService.getAllSpeakers(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/speakers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(SPEAKER_ID.toString()))
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.data[0].lastName").value("Doe"))
                .andExpect(jsonPath("$.data[0].profilePicture").value("https://example.com/avatar.jpg"))
                .andExpect(jsonPath("$.data[0].bio").value("Experienced speaker"))
                .andExpect(jsonPath("$.data[0].externalLinks[0].name").value("Twitter"))
                .andExpect(jsonPath("$.data[0].externalLinks[0].url").value("https://twitter.com/john"))
                .andExpect(jsonPath("$.meta.total").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(10));

        verify(speakerService).getAllSpeakers(eq(1), eq(10), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /speakers with custom pagination returns 200")
    void getAllSpeakers_withCustomPagination_returns200() throws Exception {
        var speakers = List.of(
                SpeakerResponseDto.builder()
                        .id(SPEAKER_ID)
                        .firstName("John")
                        .lastName("Doe")
                        .build()
        );
        var response = SpeakerListResponseDto.builder()
                .data(speakers)
                .meta(MetaDto.builder().total(1).page(2).size(5).build())
                .build();

        when(speakerService.getAllSpeakers(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/speakers")
                        .param("page", "2")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.size").value(5));

        verify(speakerService).getAllSpeakers(eq(2), eq(5), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /speakers with empty list returns 200 and empty data")
    void getAllSpeakers_whenEmpty_returns200WithEmptyList() throws Exception {
        var response = SpeakerListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(10).build())
                .build();

        when(speakerService.getAllSpeakers(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/speakers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.meta.total").value(0));

        verify(speakerService).getAllSpeakers(eq(1), eq(10), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /speakers with search filter returns 200 and filtered results")
    void getAllSpeakers_withSearchFilter_returns200() throws Exception {
        var speakers = List.of(
                SpeakerResponseDto.builder()
                        .id(SPEAKER_ID)
                        .firstName("John")
                        .lastName("Doe")
                        .build()
        );
        var response = SpeakerListResponseDto.builder()
                .data(speakers)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(speakerService.getAllSpeakers(anyInt(), anyInt(), eq("John"), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/speakers")
                        .param("search", "John")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.meta.total").value(1));

        verify(speakerService).getAllSpeakers(eq(1), eq(10), eq("John"), nullable(String.class));
    }

    @Test
    @DisplayName("GET /speakers with no external links returns 200 without externalLinks field")
    void getAllSpeakers_withNoExternalLinks_returns200() throws Exception {
        var speakers = List.of(
                SpeakerResponseDto.builder()
                        .id(SPEAKER_ID)
                        .firstName("John")
                        .lastName("Doe")
                        .build()
        );
        var response = SpeakerListResponseDto.builder()
                .data(speakers)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(speakerService.getAllSpeakers(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/speakers")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].externalLinks").doesNotExist())
                .andExpect(jsonPath("$.data[0].firstName").value("John"))
                .andExpect(jsonPath("$.data[0].lastName").value("Doe"));
    }
}

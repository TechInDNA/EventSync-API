package com.techindna.eventsyncapi.controller.events;

import com.techindna.eventsyncapi.controller.EventController;
import com.techindna.eventsyncapi.dto.EventListResponseDto;
import com.techindna.eventsyncapi.dto.EventResponseDto;
import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
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

class GetEventControllerTest {

    private final MockMvc mockMvc;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String EVENT_TITLE = "Conference 2025";
    private static final String EVENT_LOCATION = "Antananarivo";

    GetEventControllerTest() {
        eventService = mock(EventService.class);
        var controller = new EventController(eventService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /events returns 200 with paginated event list")
    void getAllEvents_withDefaultPagination_returns200AndList() throws Exception {
        var events = List.of(
                EventResponseDto.builder()
                        .id(EVENT_ID)
                        .title(EVENT_TITLE)
                        .description("A great event")
                        .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                        .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                        .location(EVENT_LOCATION)
                        .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                        .live(false)
                        .build()
        );
        var response = EventListResponseDto.builder()
                .data(events)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(eventService.getAllEvents(anyInt(), anyInt(), any(), any(), any(), any(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(EVENT_ID.toString()))
                .andExpect(jsonPath("$.data[0].title").value(EVENT_TITLE))
                .andExpect(jsonPath("$.data[0].description").value("A great event"))
                .andExpect(jsonPath("$.data[0].location").value(EVENT_LOCATION))
                .andExpect(jsonPath("$.data[0].isLive").value(false))
                .andExpect(jsonPath("$.meta.total").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(10));

        verify(eventService).getAllEvents(eq(1), eq(10), isNull(), isNull(), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /events with custom pagination returns 200")
    void getAllEvents_withCustomPagination_returns200() throws Exception {
        var events = List.of(
                EventResponseDto.builder().id(EVENT_ID).title(EVENT_TITLE).build()
        );
        var response = EventListResponseDto.builder()
                .data(events)
                .meta(MetaDto.builder().total(1).page(2).size(5).build())
                .build();

        when(eventService.getAllEvents(anyInt(), anyInt(), any(), any(), any(), any(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/events")
                        .param("page", "2")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.size").value(5));

        verify(eventService).getAllEvents(eq(2), eq(5), isNull(), isNull(), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /events with empty list returns 200 and empty data")
    void getAllEvents_whenEmpty_returns200WithEmptyList() throws Exception {
        var response = EventListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(10).build())
                .build();

        when(eventService.getAllEvents(anyInt(), anyInt(), any(), any(), any(), any(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/events")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.meta.total").value(0));

        verify(eventService).getAllEvents(eq(1), eq(10), isNull(), isNull(), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /events with title filter returns 200 and filtered results")
    void getAllEvents_withTitleFilter_returns200() throws Exception {
        var events = List.of(
                EventResponseDto.builder().id(EVENT_ID).title(EVENT_TITLE).build()
        );
        var response = EventListResponseDto.builder()
                .data(events)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(eventService.getAllEvents(anyInt(), anyInt(), eq("Conference"), any(), any(), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/events")
                        .param("title", "Conference")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value(EVENT_TITLE))
                .andExpect(jsonPath("$.meta.total").value(1));

        verify(eventService).getAllEvents(eq(1), eq(10), eq("Conference"), isNull(), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /events with location filter returns 200 and filtered results")
    void getAllEvents_withLocationFilter_returns200() throws Exception {
        var events = List.of(
                EventResponseDto.builder().id(EVENT_ID).title(EVENT_TITLE).location(EVENT_LOCATION).build()
        );
        var response = EventListResponseDto.builder()
                .data(events)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(eventService.getAllEvents(anyInt(), anyInt(), any(), eq("Antananarivo"), any(), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/events")
                        .param("location", "Antananarivo")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].location").value(EVENT_LOCATION))
                .andExpect(jsonPath("$.meta.total").value(1));

        verify(eventService).getAllEvents(eq(1), eq(10), isNull(), eq("Antananarivo"), isNull(), isNull(), isNull(), nullable(String.class));
    }
}

package com.techindna.eventsyncapi.controller.sessions;

import com.techindna.eventsyncapi.controller.SessionController;
import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.session.EventRefDto;
import com.techindna.eventsyncapi.dto.session.RoomRefDto;
import com.techindna.eventsyncapi.dto.session.SessionListResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SpeakerRefDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.service.SessionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetSessionsControllerTest {

    private final MockMvc mockMvc;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("11111111-1111-1111-1111-111111111111");
    private static final UUID EVENT_ID = UUID.fromString("22222222-2222-2222-2222-222222222222");
    private static final UUID SPEAKER_ID = UUID.fromString("33333333-3333-3333-3333-333333333333");

    GetSessionsControllerTest() {
        sessionService = mock(SessionService.class);
        var controller = new SessionController(sessionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    private SessionResponseDto sampleSession() {
        return SessionResponseDto.builder()
                .id(SESSION_ID)
                .title("Spring Boot Deep Dive")
                .description("A hands-on session")
                .startDate(Instant.parse("2026-06-01T09:00:00Z"))
                .endDate(Instant.parse("2026-06-01T10:00:00Z"))
                .room(RoomRefDto.builder().id(ROOM_ID).name("Room A").build())
                .capacity(100)
                .event(EventRefDto.builder().id(EVENT_ID).title("DevConf 2026").build())
                .speakers(List.of(SpeakerRefDto.builder()
                        .id(SPEAKER_ID)
                        .firstName("Jane")
                        .lastName("Doe")
                        .build()))
                .live(false)
                .build();
    }

    @Test
    @DisplayName("GET /sessions returns 200 with paginated session list")
    void getAllSessions_withDefaultPagination_returns200AndList() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of(sampleSession()))
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), any(), any(), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions").contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(SESSION_ID.toString()))
                .andExpect(jsonPath("$.data[0].title").value("Spring Boot Deep Dive"))
                .andExpect(jsonPath("$.data[0].room.name").value("Room A"))
                .andExpect(jsonPath("$.data[0].event.title").value("DevConf 2026"))
                .andExpect(jsonPath("$.data[0].speakers[0].firstName").value("Jane"))
                .andExpect(jsonPath("$.data[0].isLive").value(false))
                .andExpect(jsonPath("$.meta.total").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(20));

        verify(sessionService).getAllSessions(eq(1), eq(20), isNull(), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /sessions with custom pagination returns 200")
    void getAllSessions_withCustomPagination_returns200() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(2).size(5).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), any(), any(), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions").param("page", "2").param("size", "5"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.size").value(5));

        verify(sessionService).getAllSessions(eq(2), eq(5), isNull(), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /sessions with empty list returns 200 and empty data")
    void getAllSessions_whenEmpty_returns200WithEmptyList() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(20).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), any(), any(), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.meta.total").value(0));
    }

    @Test
    @DisplayName("GET /sessions with room filter forwards to service")
    void getAllSessions_withRoomFilter_returns200() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of(sampleSession()))
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), eq("Room A"), any(), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions").param("room", "Room A"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].room.name").value("Room A"));

        verify(sessionService).getAllSessions(eq(1), eq(20), eq("Room A"), isNull(), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /sessions with speaker filter forwards to service")
    void getAllSessions_withSpeakerFilter_returns200() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of(sampleSession()))
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), any(), any(), eq("Jane"), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions").param("speaker", "Jane"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].speakers[0].firstName").value("Jane"));

        verify(sessionService).getAllSessions(eq(1), eq(20), isNull(), isNull(), eq("Jane"), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /sessions with event filter forwards to service")
    void getAllSessions_withEventFilter_returns200() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of(sampleSession()))
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), any(), eq("DevConf 2026"), any(), any(), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions").param("event", "DevConf 2026"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].event.title").value("DevConf 2026"));

        verify(sessionService).getAllSessions(eq(1), eq(20), isNull(), eq("DevConf 2026"), isNull(), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /sessions with live filter forwards to service")
    void getAllSessions_withLiveFilter_returns200() throws Exception {
        var response = SessionListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(20).build())
                .build();

        when(sessionService.getAllSessions(anyInt(), anyInt(), any(), any(), any(), eq(true), nullable(String.class)))
                .thenReturn(response);

        mockMvc.perform(get("/sessions").param("live", "true"))
                .andExpect(status().isOk());

        verify(sessionService).getAllSessions(eq(1), eq(20), isNull(), isNull(), isNull(), eq(true), nullable(String.class));
    }
}
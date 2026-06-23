package com.techindna.eventsyncapi.service.sessions;

import com.techindna.eventsyncapi.dto.session.SessionListResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.QuestionMapper;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.SessionService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.SessionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetSessionsServiceTest {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID EVENT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID SPEAKER_ID = UUID.fromString("d4e5f6a7-b8c9-0123-defa-234567890123");
    private static final String TEST_IP = "127.0.0.1";

    GetSessionsServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        sessionService = new SessionService(
                sessionRepository, sessionMapper, new SessionValidator(new DataValidator()),
                mock(AuthService.class), mock(QuestionRepository.class), mock(QuestionMapper.class)
        );
    }

    private Session sampleSessionEntity() {
        var room = Room.builder().id(ROOM_ID).name("Room A").build();
        var event = Event.builder().id(EVENT_ID).title("DevConf 2026").build();
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("Jane")
                .lastName("Doe")
                .role(Role.SPEAKER)
                .build();
        return Session.builder()
                .id(SESSION_ID)
                .title("Spring Boot Deep Dive")
                .description("A hands-on session")
                .startDate(Instant.parse("2026-06-01T09:00:00Z"))
                .endDate(Instant.parse("2026-06-01T10:00:00Z"))
                .room(room)
                .event(event)
                .capacity(100)
                .speakers(List.of(speaker))
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();
    }

    private SessionResponseDto sampleSessionDto() {
        return SessionResponseDto.builder().id(SESSION_ID).title("Spring Boot Deep Dive").build();
    }

    @Test
    @DisplayName("getAllSessions returns mapped list with default pagination")
    void getAllSessions_withValidPagination_returnsList() {
        var session = sampleSessionEntity();
        when(sessionRepository.countByFilters(null, null, null, null)).thenReturn(1L);
        when(sessionRepository.findByFilters(null, null, null, null, 20, 0)).thenReturn(List.of(session));
        when(sessionRepository.findAllByIdInWithDetails(List.of(SESSION_ID))).thenReturn(List.of(session));
        when(sessionMapper.toListResponseDto(List.of(session), 1L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of(sampleSessionDto()))
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(1).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, null, null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(20, result.getMeta().getSize());
        assertEquals(1, result.getData().size());
        assertEquals(SESSION_ID, result.getData().getFirst().getId());

        verify(sessionRepository).countByFilters(null, null, null, null);
        verify(sessionRepository).findByFilters(null, null, null, null, 20, 0);
        verify(sessionRepository).findAllByIdInWithDetails(List.of(SESSION_ID));
    }

    @Test
    @DisplayName("getAllSessions with page 2 returns correct offset")
    void getAllSessions_withPage2_returnsCorrectOffset() {
        when(sessionRepository.countByFilters(null, null, null, null)).thenReturn(5L);
        when(sessionRepository.findByFilters(null, null, null, null, 20, 20)).thenReturn(List.of());
        when(sessionMapper.toListResponseDto(List.of(), 5L, 2, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of())
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(5).page(2).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(2, 20, null, null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(5, result.getMeta().getTotal());
        assertEquals(2, result.getMeta().getPage());
        assertTrue(result.getData().isEmpty());

        verify(sessionRepository).findByFilters(null, null, null, null, 20, 20);
    }

    @Test
    @DisplayName("getAllSessions with page < 1 defaults to 1")
    void getAllSessions_withInvalidPage_defaultsTo1() {
        when(sessionRepository.countByFilters(null, null, null, null)).thenReturn(0L);
        when(sessionRepository.findByFilters(null, null, null, null, 20, 0)).thenReturn(List.of());
        when(sessionMapper.toListResponseDto(List.of(), 0L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of())
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(0).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(0, 20, null, null, null, null, TEST_IP);

        assertEquals(1, result.getMeta().getPage());
        verify(sessionRepository).findByFilters(null, null, null, null, 20, 0);
    }

    @Test
    @DisplayName("getAllSessions with size < 1 defaults to 20")
    void getAllSessions_withInvalidSize_defaultsTo20() {
        when(sessionRepository.countByFilters(null, null, null, null)).thenReturn(0L);
        when(sessionRepository.findByFilters(null, null, null, null, 20, 0)).thenReturn(List.of());
        when(sessionMapper.toListResponseDto(List.of(), 0L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of())
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(0).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 0, null, null, null, null, TEST_IP);

        assertEquals(20, result.getMeta().getSize());
        verify(sessionRepository).findByFilters(null, null, null, null, 20, 0);
    }

    @Test
    @DisplayName("getAllSessions with empty database returns empty list")
    void getAllSessions_withNoSessions_returnsEmptyList() {
        when(sessionRepository.countByFilters(null, null, null, null)).thenReturn(0L);
        when(sessionRepository.findByFilters(null, null, null, null, 20, 0)).thenReturn(List.of());
        when(sessionMapper.toListResponseDto(List.of(), 0L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of())
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(0).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, null, null, null, null, TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(sessionRepository, never()).findAllByIdInWithDetails(any());
    }

    @Test
    @DisplayName("getAllSessions with room filter forwards to repository")
    void getAllSessions_withRoomFilter_returnsMatchingSessions() {
        var session = sampleSessionEntity();
        when(sessionRepository.countByFilters("Room A", null, null, null)).thenReturn(1L);
        when(sessionRepository.findByFilters("Room A", null, null, null, 20, 0)).thenReturn(List.of(session));
        when(sessionRepository.findAllByIdInWithDetails(List.of(SESSION_ID))).thenReturn(List.of(session));
        when(sessionMapper.toListResponseDto(List.of(session), 1L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of(sampleSessionDto()))
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(1).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, "Room A", null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());

        verify(sessionRepository).countByFilters("Room A", null, null, null);
        verify(sessionRepository).findByFilters("Room A", null, null, null, 20, 0);
    }

    @Test
    @DisplayName("getAllSessions with speaker filter forwards to repository")
    void getAllSessions_withSpeakerFilter_returnsMatchingSessions() {
        var session = sampleSessionEntity();
        when(sessionRepository.countByFilters(null, null, "Jane", null)).thenReturn(1L);
        when(sessionRepository.findByFilters(null, null, "Jane", null, 20, 0)).thenReturn(List.of(session));
        when(sessionRepository.findAllByIdInWithDetails(List.of(SESSION_ID))).thenReturn(List.of(session));
        when(sessionMapper.toListResponseDto(List.of(session), 1L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of(sampleSessionDto()))
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(1).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, null, null, "Jane", null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());

        verify(sessionRepository).countByFilters(null, null, "Jane", null);
        verify(sessionRepository).findByFilters(null, null, "Jane", null, 20, 0);
    }

    @Test
    @DisplayName("getAllSessions with event filter forwards to repository")
    void getAllSessions_withEventFilter_returnsMatchingSessions() {
        var session = sampleSessionEntity();
        when(sessionRepository.countByFilters(null, "DevConf 2026", null, null)).thenReturn(1L);
        when(sessionRepository.findByFilters(null, "DevConf 2026", null, null, 20, 0)).thenReturn(List.of(session));
        when(sessionRepository.findAllByIdInWithDetails(List.of(SESSION_ID))).thenReturn(List.of(session));
        when(sessionMapper.toListResponseDto(List.of(session), 1L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of(sampleSessionDto()))
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(1).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, null, "DevConf 2026", null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());

        verify(sessionRepository).countByFilters(null, "DevConf 2026", null, null);
        verify(sessionRepository).findByFilters(null, "DevConf 2026", null, null, 20, 0);
    }

    @Test
    @DisplayName("getAllSessions with live=true filter forwards to repository")
    void getAllSessions_withLiveFilter_returnsMatchingSessions() {
        when(sessionRepository.countByFilters(null, null, null, true)).thenReturn(0L);
        when(sessionRepository.findByFilters(null, null, null, true, 20, 0)).thenReturn(List.of());
        when(sessionMapper.toListResponseDto(List.of(), 0L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of())
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(0).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, null, null, null, true, TEST_IP);

        assertNotNull(result);
        assertTrue(result.getData().isEmpty());

        verify(sessionRepository).countByFilters(null, null, null, true);
    }

    @Test
    @DisplayName("getAllSessions with blank room string passes validation and returns results")
    void getAllSessions_withBlankRoom_passesValidation() {
        var session = sampleSessionEntity();
        when(sessionRepository.countByFilters("", null, null, null)).thenReturn(1L);
        when(sessionRepository.findByFilters("", null, null, null, 20, 0)).thenReturn(List.of(session));
        when(sessionRepository.findAllByIdInWithDetails(List.of(SESSION_ID))).thenReturn(List.of(session));
        when(sessionMapper.toListResponseDto(List.of(session), 1L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of(sampleSessionDto()))
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(1).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, "", null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("getAllSessions with blank speaker string passes validation and returns results")
    void getAllSessions_withBlankSpeaker_passesValidation() {
        var session = sampleSessionEntity();
        when(sessionRepository.countByFilters(null, null, "", null)).thenReturn(1L);
        when(sessionRepository.findByFilters(null, null, "", null, 20, 0)).thenReturn(List.of(session));
        when(sessionRepository.findAllByIdInWithDetails(List.of(SESSION_ID))).thenReturn(List.of(session));
        when(sessionMapper.toListResponseDto(List.of(session), 1L, 1, 20))
                .thenReturn(SessionListResponseDto.builder()
                        .data(List.of(sampleSessionDto()))
                        .meta(com.techindna.eventsyncapi.dto.MetaDto.builder().total(1).page(1).size(20).build())
                        .build());

        var result = sessionService.getAllSessions(1, 20, null, null, "", null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
    }

    @Test
    @DisplayName("getAllSessions with invalid room string throws UnprocessableEntityException")
    void getAllSessions_withInvalidRoom_throwsException() {
        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.getAllSessions(1, 20, "@@invalid@@", null, null, null, TEST_IP));
    }

    @Test
    @DisplayName("getAllSessions with invalid speaker string throws UnprocessableEntityException")
    void getAllSessions_withInvalidSpeaker_throwsException() {
        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.getAllSessions(1, 20, null, null, "@@invalid@@", null, TEST_IP));
    }

    @Test
    @DisplayName("getAllSessions with invalid event string throws UnprocessableEntityException")
    void getAllSessions_withInvalidEvent_throwsException() {
        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.getAllSessions(1, 20, null, "@@invalid@@", null, null, TEST_IP));
    }
}
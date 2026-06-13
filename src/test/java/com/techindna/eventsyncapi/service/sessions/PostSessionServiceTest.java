package com.techindna.eventsyncapi.service.sessions;

import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.RoomEventExistence;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.service.SessionService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.SessionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostSessionServiceTest {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID EVENT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final String SESSION_TITLE = "Keynote Speech";

    PostSessionServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        sessionService = new SessionService(
                sessionRepository, sessionMapper, new SessionValidator(new DataValidator())
        );
    }

    @Test
    @DisplayName("with valid input inserts and returns mapped response")
    void withValidInput_returnsCreatedSession() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("An inspiring keynote speech")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        var existence = mock(RoomEventExistence.class);
        when(existence.getRoomId()).thenReturn(ROOM_ID);
        when(existence.getEventId()).thenReturn(EVENT_ID);
        when(sessionRepository.findRoomAndEventExistence(ROOM_ID, EVENT_ID)).thenReturn(existence);

        var session = Session.builder().id(SESSION_ID).title(SESSION_TITLE).build();
        when(sessionRepository.insertSession(
                SESSION_TITLE, "An inspiring keynote speech",
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"),
                ROOM_ID, 100, EVENT_ID
        )).thenReturn(Optional.of(session));

        var expectedResponse = SessionResponseDto.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .capacity(100)
                .build();
        when(sessionMapper.toResponseDto(any())).thenReturn(expectedResponse);

        var result = sessionService.createSession(request);

        assertNotNull(result);
        assertEquals(SESSION_ID, result.getId());
        assertEquals(SESSION_TITLE, result.getTitle());
        assertEquals(100, result.getCapacity());

        verify(sessionRepository).findRoomAndEventExistence(ROOM_ID, EVENT_ID);
        verify(sessionRepository).insertSession(
                SESSION_TITLE, "An inspiring keynote speech",
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"),
                ROOM_ID, 100, EVENT_ID
        );
        verify(sessionMapper).toResponseDto(any());
    }

    @Test
    @DisplayName("with null title throws UnprocessableEntityException")
    void withNullTitle_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(null)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with empty title throws UnprocessableEntityException")
    void withEmptyTitle_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title("")
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with null description throws UnprocessableEntityException")
    void withNullDescription_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description(null)
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with null startDate throws UnprocessableEntityException")
    void withNullStartDate_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(null)
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with null endDate throws UnprocessableEntityException")
    void withNullEndDate_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(null)
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with endDate before startDate throws UnprocessableEntityException")
    void withEndDateBeforeStartDate_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T10:00:00Z"))
                .endDate(Instant.parse("2025-06-01T09:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with endDate equal to startDate throws UnprocessableEntityException")
    void withEndDateEqualToStartDate_throwsUnprocessable() {
        var now = Instant.parse("2025-06-01T10:00:00Z");
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(now)
                .endDate(now)
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with negative capacity throws UnprocessableEntityException")
    void withNegativeCapacity_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(-1)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with null roomId throws UnprocessableEntityException")
    void withNullRoomId_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(null)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with null eventId throws UnprocessableEntityException")
    void withNullEventId_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(null)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with non-existent room or event ID throws NotFoundException")
    void withNonExistentRoomOrEvent_throwsNotFound() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        var existence = mock(RoomEventExistence.class);
        when(existence.getRoomId()).thenReturn(null);
        when(existence.getEventId()).thenReturn(null);
        when(sessionRepository.findRoomAndEventExistence(ROOM_ID, EVENT_ID)).thenReturn(existence);

        var exception = assertThrows(NotFoundException.class, () -> sessionService.createSession(request));

        assertEquals(
                String.format("Room (%s) or event (%s) not found.", ROOM_ID, EVENT_ID),
                exception.getMessage()
        );

        verify(sessionRepository).findRoomAndEventExistence(ROOM_ID, EVENT_ID);
        verifyNoInteractions(sessionMapper);
        verify(sessionRepository, never()).insertSession(any(), any(), any(), any(), any(), anyInt(), any());
    }

    @Test
    @DisplayName("with duplicate title throws ConflictException")
    void withDuplicateTitle_throwsConflict() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(100)
                .eventId(EVENT_ID)
                .build();

        var existence = mock(RoomEventExistence.class);
        when(existence.getRoomId()).thenReturn(ROOM_ID);
        when(existence.getEventId()).thenReturn(EVENT_ID);
        when(sessionRepository.findRoomAndEventExistence(ROOM_ID, EVENT_ID)).thenReturn(existence);

        when(sessionRepository.insertSession(
                SESSION_TITLE, "Desc",
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"),
                ROOM_ID, 100, EVENT_ID
        )).thenReturn(Optional.empty());

        var exception = assertThrows(ConflictException.class, () -> sessionService.createSession(request));
        assertEquals("Session 'Keynote Speech' already exists.", exception.getMessage());

        verify(sessionRepository).findRoomAndEventExistence(ROOM_ID, EVENT_ID);
        verify(sessionRepository).insertSession(
                SESSION_TITLE, "Desc",
                Instant.parse("2025-06-01T09:00:00Z"),
                Instant.parse("2025-06-01T10:00:00Z"),
                ROOM_ID, 100, EVENT_ID
        );
        verifyNoInteractions(sessionMapper);
    }

    @Test
    @DisplayName("with null capacity throws UnprocessableEntityException")
    void withNullCapacity_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(null)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("with zero capacity throws UnprocessableEntityException")
    void withZeroCapacity_throwsUnprocessable() {
        var request = SessionInputDto.builder()
                .title(SESSION_TITLE)
                .description("Desc")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .roomId(ROOM_ID)
                .capacity(0)
                .eventId(EVENT_ID)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> sessionService.createSession(request));
        verifyNoInteractions(sessionRepository, sessionMapper);
    }
}

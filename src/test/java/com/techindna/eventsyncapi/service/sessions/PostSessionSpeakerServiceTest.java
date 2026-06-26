package com.techindna.eventsyncapi.service.sessions;

import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostSessionSpeakerServiceTest {

    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEAKER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final String START_TIME = "2026-07-02T10:00:00+03:00";
    private static final String END_TIME = "2026-07-02T11:30:00+03:00";

    PostSessionSpeakerServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        sessionService = new SessionService(
                sessionRepository, mock(SessionMapper.class), new SessionValidator(new DataValidator()),
                mock(AuthService.class), mock(QuestionRepository.class), mock(QuestionMapper.class)
        );
    }

    @Test
    @DisplayName("with valid input inserts session-speaker link and returns success message")
    void withValidInput_returnsSuccessMessage() {
        var request = SessionSpeakerInputDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .build();

        when(sessionRepository.existsOverlappingSpeakerInRoom(SESSION_ID, START_TIME, END_TIME)).thenReturn(false);
        when(sessionRepository.insertSessionSpeaker(SESSION_ID, SPEAKER_ID, START_TIME, END_TIME))
                .thenReturn(Optional.of(SESSION_ID));

        String result = sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request);

        assertEquals("Speaker linked to session.", result);
        verify(sessionRepository).existsOverlappingSpeakerInRoom(SESSION_ID, START_TIME, END_TIME);
        verify(sessionRepository).insertSessionSpeaker(SESSION_ID, SPEAKER_ID, START_TIME, END_TIME);
    }

    @Test
    @DisplayName("with null startTime throws UnprocessableEntityException")
    void withNullStartTime_throwsUnprocessable() {
        var request = SessionSpeakerInputDto.builder()
                .startTime(null)
                .endTime(END_TIME)
                .build();

        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("with null endTime throws UnprocessableEntityException")
    void withNullEndTime_throwsUnprocessable() {
        var request = SessionSpeakerInputDto.builder()
                .startTime(START_TIME)
                .endTime(null)
                .build();

        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("with endTime before startTime throws UnprocessableEntityException")
    void withEndTimeBeforeStartTime_throwsUnprocessable() {
        var request = SessionSpeakerInputDto.builder()
                .startTime("2026-07-02T14:00:00+03:00")
                .endTime("2026-07-02T10:00:00+03:00")
                .build();

        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("with endTime equal to startTime throws UnprocessableEntityException")
    void withEndTimeEqualToStartTime_throwsUnprocessable() {
        var request = SessionSpeakerInputDto.builder()
                .startTime(START_TIME)
                .endTime(START_TIME)
                .build();

        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("with invalid startTime format throws UnprocessableEntityException")
    void withInvalidStartTimeFormat_throwsUnprocessable() {
        var request = SessionSpeakerInputDto.builder()
                .startTime("not-a-time")
                .endTime(END_TIME)
                .build();

        assertThrows(UnprocessableEntityException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));
        verifyNoInteractions(sessionRepository);
    }

    @Test
    @DisplayName("with non-existent session or speaker throws NotFoundException")
    void withNonExistentSessionOrSpeaker_throwsNotFound() {
        var request = SessionSpeakerInputDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .build();

        when(sessionRepository.existsOverlappingSpeakerInRoom(SESSION_ID, START_TIME, END_TIME)).thenReturn(false);
        when(sessionRepository.insertSessionSpeaker(SESSION_ID, SPEAKER_ID, START_TIME, END_TIME))
                .thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));

        assertEquals(
                String.format("Session (%s) or speaker (%s) not found.", SESSION_ID, SPEAKER_ID),
                exception.getMessage()
        );
        verify(sessionRepository).existsOverlappingSpeakerInRoom(SESSION_ID, START_TIME, END_TIME);
        verify(sessionRepository).insertSessionSpeaker(SESSION_ID, SPEAKER_ID, START_TIME, END_TIME);
    }

    @Test
    @DisplayName("with busy room throws ConflictException")
    void withBusyRoom_throwsConflict() {
        var request = SessionSpeakerInputDto.builder()
                .startTime(START_TIME)
                .endTime(END_TIME)
                .build();

        when(sessionRepository.existsOverlappingSpeakerInRoom(SESSION_ID, START_TIME, END_TIME)).thenReturn(true);

        var exception = assertThrows(ConflictException.class,
                () -> sessionService.addSpeakerToSession(SESSION_ID, SPEAKER_ID, request));

        assertEquals("The room is already occupied during the requested time slot.", exception.getMessage());
        verify(sessionRepository).existsOverlappingSpeakerInRoom(SESSION_ID, START_TIME, END_TIME);
        verifyNoMoreInteractions(sessionRepository);
    }
}

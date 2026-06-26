package com.techindna.eventsyncapi.service.sessions;

import com.techindna.eventsyncapi.dto.session.SessionSpeakerTimeSlotDto;
import com.techindna.eventsyncapi.exception.NotFoundException;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetSessionSpeakerServiceTest {

    private final SessionRepository sessionRepository;
    private final AuthService authService;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEAKER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final String TEST_IP = "127.0.0.1";

    GetSessionSpeakerServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        authService = mock(AuthService.class);
        sessionService = new SessionService(
                sessionRepository, mock(SessionMapper.class), new SessionValidator(new DataValidator()),
                authService, mock(QuestionRepository.class), mock(QuestionMapper.class)
        );
    }

    @Test
    @DisplayName("getSessionSpeakerTimeSlots with existing link returns list of time slots")
    void getSessionSpeakerTimeSlots_withExistingLink_returnsTimeSlots() {
        when(sessionRepository.findSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID))
                .thenReturn(List.<Object[]>of(
                        new Object[]{"a1b2c3d4-e5f6-7890-abcd-ef1234567890", "2026-08-02 14:00:00+03", "2026-08-02 15:30:00+03"}
                ));

        List<SessionSpeakerTimeSlotDto> result = sessionService.getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, TEST_IP);

        assertEquals(1, result.size());
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", result.getFirst().getId());
        assertEquals("2026-08-02 14:00:00+03", result.getFirst().getStartTime());
        assertEquals("2026-08-02 15:30:00+03", result.getFirst().getEndTime());
        verify(sessionRepository).findSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID);
        verify(authService).checkBlacklist(TEST_IP);
        verifyNoMoreInteractions(sessionRepository);
    }

    @Test
    @DisplayName("getSessionSpeakerTimeSlots with multiple time slots returns all of them")
    void getSessionSpeakerTimeSlots_withMultipleTimeSlots_returnsAll() {
        when(sessionRepository.findSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID))
                .thenReturn(List.of(
                        new Object[]{"a1b2c3d4-e5f6-7890-abcd-ef1234567890", "2026-08-02 10:00:00+03", "2026-08-02 11:00:00+03"},
                        new Object[]{"b2c3d4e5-f6a7-8901-bcde-f12345678901", "2026-08-02 14:00:00+03", "2026-08-02 15:30:00+03"}
                ));

        List<SessionSpeakerTimeSlotDto> result = sessionService.getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, TEST_IP);

        assertEquals(2, result.size());
        assertEquals("a1b2c3d4-e5f6-7890-abcd-ef1234567890", result.get(0).getId());
        assertEquals("2026-08-02 10:00:00+03", result.get(0).getStartTime());
        assertEquals("b2c3d4e5-f6a7-8901-bcde-f12345678901", result.get(1).getId());
        assertEquals("2026-08-02 14:00:00+03", result.get(1).getStartTime());
    }

    @Test
    @DisplayName("getSessionSpeakerTimeSlots with no link throws NotFoundException")
    void getSessionSpeakerTimeSlots_withNoLink_throwsNotFound() {
        when(sessionRepository.findSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID))
                .thenReturn(List.of());

        var exception = assertThrows(NotFoundException.class,
                () -> sessionService.getSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID, TEST_IP));

        assertTrue(exception.getMessage().contains("Speaker"));
        assertTrue(exception.getMessage().contains(SESSION_ID.toString()));
        verify(sessionRepository).findSessionSpeakerTimeSlots(SESSION_ID, SPEAKER_ID);
        verify(authService).checkBlacklist(TEST_IP);
        verifyNoMoreInteractions(sessionRepository);
    }
}

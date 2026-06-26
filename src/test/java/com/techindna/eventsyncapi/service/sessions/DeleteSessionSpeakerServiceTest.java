package com.techindna.eventsyncapi.service.sessions;

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

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteSessionSpeakerServiceTest {

    private final SessionRepository sessionRepository;
    private final SessionService sessionService;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID SPEAKER_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");

    DeleteSessionSpeakerServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        sessionService = new SessionService(
                sessionRepository, mock(SessionMapper.class), new SessionValidator(new DataValidator()),
                mock(AuthService.class), mock(QuestionRepository.class), mock(QuestionMapper.class)
        );
    }

    @Test
    @DisplayName("deleteSpeakerFromSession with existing link deletes and returns void")
    void deleteSpeakerFromSession_withExistingLink_deletesSuccessfully() {
        when(sessionRepository.deleteSessionSpeaker(SESSION_ID, SPEAKER_ID))
                .thenReturn(Optional.of(SESSION_ID));

        assertDoesNotThrow(() -> sessionService.deleteSpeakerFromSession(SESSION_ID, SPEAKER_ID));
        verify(sessionRepository).deleteSessionSpeaker(SESSION_ID, SPEAKER_ID);
    }

    @Test
    @DisplayName("deleteSpeakerFromSession with non-existent link throws NotFoundException")
    void deleteSpeakerFromSession_withNonExistentLink_throwsNotFound() {
        when(sessionRepository.deleteSessionSpeaker(SESSION_ID, SPEAKER_ID))
                .thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> sessionService.deleteSpeakerFromSession(SESSION_ID, SPEAKER_ID));

        assertEquals(
                String.format("Speaker %s or session %s not found.", SPEAKER_ID, SESSION_ID),
                exception.getMessage()
        );
        verify(sessionRepository).deleteSessionSpeaker(SESSION_ID, SPEAKER_ID);
    }
}

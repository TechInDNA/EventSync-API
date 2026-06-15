package com.techindna.eventsyncapi.service.speakers;

import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.ExternalLinkMapper;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.mapper.SpeakerMapper;
import com.techindna.eventsyncapi.repository.ExternalLinkRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.SpeakerService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.ExternalLinkValidator;
import com.techindna.eventsyncapi.validator.SpeakerValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DeleteSpeakerServiceTest {

    private final UserRepository userRepository;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    DeleteSpeakerServiceTest() {
        userRepository = mock(UserRepository.class);
        speakerService = new SpeakerService(
                userRepository, mock(ExternalLinkRepository.class), new ExternalLinkValidator(new DataValidator()), new SpeakerMapper(new ExternalLinkMapper()),
                mock(SessionRepository.class), mock(SessionMapper.class), mock(AuthService.class),
                new SpeakerValidator(new DataValidator())
        );
    }

    @Test
    @DisplayName("deleteSpeaker with existing id deletes and returns void")
    void deleteSpeaker_withExistingId_deletesSuccessfully() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();
        when(userRepository.deleteSpeakerById(SPEAKER_ID)).thenReturn(Optional.of(speaker));

        assertDoesNotThrow(() -> speakerService.deleteSpeaker(SPEAKER_ID));

        verify(userRepository).deleteSpeakerById(SPEAKER_ID);
    }

    @Test
    @DisplayName("deleteSpeaker with unknown id throws NotFoundException")
    void deleteSpeaker_withUnknownId_throwsNotFound() {
        when(userRepository.deleteSpeakerById(SPEAKER_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> speakerService.deleteSpeaker(SPEAKER_ID));
        assertEquals("Speaker " + SPEAKER_ID + " not found.", exception.getMessage());

        verify(userRepository).deleteSpeakerById(SPEAKER_ID);
    }
}

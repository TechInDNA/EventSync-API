package com.techindna.eventsyncapi.service.speakers;

import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.Session;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetSpeakerByIdServiceTest {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final AuthService authService;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String TEST_IP = "127.0.0.1";

    GetSpeakerByIdServiceTest() {
        userRepository = mock(UserRepository.class);
        externalLinkRepository = mock(ExternalLinkRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        authService = mock(AuthService.class);
        speakerService = new SpeakerService(
                userRepository, externalLinkRepository, new DataValidator(),
                new SpeakerMapper(new ExternalLinkMapper()),
                sessionRepository, sessionMapper, authService
        );
    }

    @Test
    @DisplayName("getSpeakerById with existing id returns speaker details with external links and sessions")
    void getSpeakerById_withExistingId_returnsSpeakerDetails() {
        var link = ExternalLink.builder()
                .id(UUID.randomUUID())
                .name("Twitter")
                .url("https://twitter.com/john")
                .build();

        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .profilePicture("https://example.com/avatar.jpg")
                .bio("Experienced speaker")
                .role(Role.SPEAKER)
                .build();
        speaker.setExternalLinks(List.of(link));

        var session = Session.builder()
                .id(UUID.randomUUID())
                .title("Keynote")
                .build();

        when(userRepository.findByIdWithExternalLinks(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(sessionRepository.findBySpeakerId(SPEAKER_ID)).thenReturn(List.of(session));

        var result = speakerService.getSpeakerById(SPEAKER_ID, TEST_IP);

        assertNotNull(result);
        assertEquals(SPEAKER_ID, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("https://example.com/avatar.jpg", result.getProfilePicture());
        assertEquals("Experienced speaker", result.getBio());
        assertNotNull(result.getExternalLinks());
        assertEquals(1, result.getExternalLinks().size());
        assertEquals("Twitter", result.getExternalLinks().get(0).getName());
        assertNotNull(result.getSessions());

        verify(authService).checkBlacklist(TEST_IP);
        verify(userRepository).findByIdWithExternalLinks(SPEAKER_ID);
        verify(sessionRepository).findBySpeakerId(SPEAKER_ID);
        verify(sessionMapper).toSpeakerSessionDto(session);
        verifyNoMoreInteractions(authService);
    }

    @Test
    @DisplayName("getSpeakerById with unknown id throws NotFoundException")
    void getSpeakerById_withUnknownId_throwsNotFound() {
        when(userRepository.findByIdWithExternalLinks(SPEAKER_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> speakerService.getSpeakerById(SPEAKER_ID, TEST_IP));

        assertEquals("Speaker " + SPEAKER_ID + " not found.", exception.getMessage());
        verify(authService).checkBlacklist(TEST_IP);
        verify(userRepository).findByIdWithExternalLinks(SPEAKER_ID);
        verifyNoMoreInteractions(userRepository, authService);
        verifyNoInteractions(sessionRepository, sessionMapper, externalLinkRepository);
    }

    @Test
    @DisplayName("getSpeakerById with no external links returns speaker without externalLinks")
    void getSpeakerById_withNoExternalLinks_returnsSpeakerWithoutLinks() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findByIdWithExternalLinks(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(sessionRepository.findBySpeakerId(SPEAKER_ID)).thenReturn(List.of());

        var result = speakerService.getSpeakerById(SPEAKER_ID, TEST_IP);

        assertNotNull(result);
        assertEquals(SPEAKER_ID, result.getId());
        assertNull(result.getExternalLinks());
        assertNull(result.getSessions());

        verify(authService).checkBlacklist(TEST_IP);
        verify(userRepository).findByIdWithExternalLinks(SPEAKER_ID);
        verify(sessionRepository).findBySpeakerId(SPEAKER_ID);
    }

    @Test
    @DisplayName("getSpeakerById with no sessions returns speaker with empty sessions list")
    void getSpeakerById_withNoSessions_returnsSpeakerWithEmptySessions() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findByIdWithExternalLinks(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(sessionRepository.findBySpeakerId(SPEAKER_ID)).thenReturn(List.of());

        var result = speakerService.getSpeakerById(SPEAKER_ID, TEST_IP);

        assertNotNull(result);
        assertNull(result.getSessions());
        assertNull(result.getExternalLinks());

        verify(authService).checkBlacklist(TEST_IP);
        verify(userRepository).findByIdWithExternalLinks(SPEAKER_ID);
        verify(sessionRepository).findBySpeakerId(SPEAKER_ID);
    }
}

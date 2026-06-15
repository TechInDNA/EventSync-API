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
import com.techindna.eventsyncapi.service.SpeakerService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
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
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    GetSpeakerByIdServiceTest() {
        userRepository = mock(UserRepository.class);
        externalLinkRepository = mock(ExternalLinkRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        speakerService = new SpeakerService(
                userRepository, externalLinkRepository, new DataValidator(),
                new SpeakerMapper(new ExternalLinkMapper()),
                sessionRepository, sessionMapper
        );
    }

    @Test
    @DisplayName("getSpeakerById with existing id returns speaker details with external links and sessions")
    void getSpeakerById_withExistingId_returnsSpeakerDetails() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .profilePicture("https://example.com/avatar.jpg")
                .bio("Experienced speaker")
                .role(Role.SPEAKER)
                .build();

        var link = ExternalLink.builder()
                .id(UUID.randomUUID())
                .name("Twitter")
                .url("https://twitter.com/john")
                .build();

        var session = Session.builder()
                .id(UUID.randomUUID())
                .title("Keynote")
                .build();

        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(externalLinkRepository.findByUserId(SPEAKER_ID)).thenReturn(List.of(link));
        when(sessionRepository.findBySpeakerId(SPEAKER_ID)).thenReturn(List.of(session));

        var result = speakerService.getSpeakerById(SPEAKER_ID);

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

        verify(userRepository).findById(SPEAKER_ID);
        verify(externalLinkRepository).findByUserId(SPEAKER_ID);
        verify(sessionRepository).findBySpeakerId(SPEAKER_ID);
        verify(sessionMapper).toSpeakerSessionDto(session);
    }

    @Test
    @DisplayName("getSpeakerById with unknown id throws NotFoundException")
    void getSpeakerById_withUnknownId_throwsNotFound() {
        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> speakerService.getSpeakerById(SPEAKER_ID));

        assertEquals("Speaker " + SPEAKER_ID + " not found.", exception.getMessage());
        verify(userRepository).findById(SPEAKER_ID);
        verifyNoInteractions(externalLinkRepository, sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("getSpeakerById with no external links returns speaker without externalLinks")
    void getSpeakerById_withNoExternalLinks_returnsSpeakerWithoutLinks() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(externalLinkRepository.findByUserId(SPEAKER_ID)).thenReturn(List.of());
        when(sessionRepository.findBySpeakerId(SPEAKER_ID)).thenReturn(List.of());

        var result = speakerService.getSpeakerById(SPEAKER_ID);

        assertNotNull(result);
        assertEquals(SPEAKER_ID, result.getId());
        assertNull(result.getExternalLinks());
        assertNotNull(result.getSessions());
        assertTrue(result.getSessions().isEmpty());
    }

    @Test
    @DisplayName("getSpeakerById with no sessions returns speaker with empty sessions list")
    void getSpeakerById_withNoSessions_returnsSpeakerWithEmptySessions() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .build();

        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(externalLinkRepository.findByUserId(SPEAKER_ID)).thenReturn(List.of());
        when(sessionRepository.findBySpeakerId(SPEAKER_ID)).thenReturn(List.of());

        var result = speakerService.getSpeakerById(SPEAKER_ID);

        assertNotNull(result);
        assertNotNull(result.getSessions());
        assertTrue(result.getSessions().isEmpty());
    }
}

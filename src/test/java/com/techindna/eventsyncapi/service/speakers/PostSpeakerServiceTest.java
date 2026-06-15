package com.techindna.eventsyncapi.service.speakers;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PostSpeakerServiceTest {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final AuthService authService;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PostSpeakerServiceTest() {
        userRepository = mock(UserRepository.class);
        externalLinkRepository = mock(ExternalLinkRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        authService = mock(AuthService.class);
        speakerService = new SpeakerService(
                userRepository, externalLinkRepository, new DataValidator(), new SpeakerMapper(new ExternalLinkMapper()),
                sessionRepository, sessionMapper, authService
        );
    }

    @Test
    @DisplayName("with valid input inserts speaker and returns mapped response")
    void withValidInput_createsSpeaker() {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .build();

        var savedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.insertSpeaker(any(User.class))).thenReturn(Optional.of(savedUser));

        var result = speakerService.createSpeaker(request);

        assertNotNull(result);
        assertEquals(SPEAKER_ID, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("https://example.com/avatar.jpg", result.getProfilePicture());
        assertNull(result.getBio());
        assertTrue(result.getExternalLinks() == null || result.getExternalLinks().isEmpty());

        verify(userRepository).insertSpeaker(any(User.class));
        verifyNoInteractions(externalLinkRepository);
    }

    @Test
    @DisplayName("with duplicate email throws ConflictException")
    void withDuplicateEmail_throwsConflict() {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .build();

        when(userRepository.insertSpeaker(any(User.class)))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ConflictException.class,
                () -> speakerService.createSpeaker(request));

        assertEquals("Email existing@example.com already exists.", exception.getMessage());
        verify(userRepository).insertSpeaker(any(User.class));
    }

    @Test
    @DisplayName("with null firstName throws UnprocessableEntityException")
    void withNullFirstName_throwsUnprocessable() {
        var request = SpeakerInputDto.builder()
                .firstName(null)
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("with empty firstName throws UnprocessableEntityException")
    void withEmptyFirstName_throwsUnprocessable() {
        var request = SpeakerInputDto.builder()
                .firstName("")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("with null lastName throws UnprocessableEntityException")
    void withNullLastName_throwsUnprocessable() {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName(null)
                .email("john@example.com")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("with invalid email throws UnprocessableEntityException")
    void withInvalidEmail_throwsUnprocessable() {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("not-an-email")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("with external links saves them and includes in response")
    void withExternalLinks_savesAndReturnsThem() {
        var links = List.of(
                new ExternalLinkDto("Twitter", "https://twitter.com/john"),
                new ExternalLinkDto("GitHub", "https://github.com/john")
        );

        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .externalLinks(links)
                .build();

        var savedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        var savedLink1 = ExternalLink.builder()
                .id(UUID.randomUUID())
                .name("Twitter")
                .url("https://twitter.com/john")
                .user(savedUser)
                .build();

        var savedLink2 = ExternalLink.builder()
                .id(UUID.randomUUID())
                .name("GitHub")
                .url("https://github.com/john")
                .user(savedUser)
                .build();

        when(userRepository.insertSpeaker(any(User.class)))
                .thenReturn(Optional.of(savedUser));
        when(externalLinkRepository.insertExternalLink(eq(SPEAKER_ID), eq("Twitter"), eq("https://twitter.com/john")))
                .thenReturn(Optional.of(savedLink1));
        when(externalLinkRepository.insertExternalLink(eq(SPEAKER_ID), eq("GitHub"), eq("https://github.com/john")))
                .thenReturn(Optional.of(savedLink2));

        var result = speakerService.createSpeaker(request);

        assertNotNull(result);
        assertEquals("John", result.getFirstName());
        assertNotNull(result.getExternalLinks());
        assertEquals(2, result.getExternalLinks().size());
        assertEquals("Twitter", result.getExternalLinks().get(0).getName());
        assertEquals("https://github.com/john", result.getExternalLinks().get(1).getUrl());

        verify(externalLinkRepository).insertExternalLink(eq(SPEAKER_ID), eq("Twitter"), eq("https://twitter.com/john"));
        verify(externalLinkRepository).insertExternalLink(eq(SPEAKER_ID), eq("GitHub"), eq("https://github.com/john"));
    }

    @Test
    @DisplayName("with duplicate external link url throws ConflictException")
    void withDuplicateUrl_throwsConflict() {
        var links = List.of(new ExternalLinkDto("Twitter", "https://twitter.com/john"));
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .externalLinks(links)
                .build();

        var savedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.insertSpeaker(any(User.class)))
                .thenReturn(Optional.of(savedUser));
        when(externalLinkRepository.insertExternalLink(any(), any(), any()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ConflictException.class,
                () -> speakerService.createSpeaker(request));

        assertEquals("URL https://twitter.com/john already exists.", exception.getMessage());
        verify(externalLinkRepository).insertExternalLink(eq(SPEAKER_ID), eq("Twitter"), eq("https://twitter.com/john"));
    }

    @Test
    @DisplayName("with bio and profilePicture includes them in response")
    void withBioAndProfilePicture_returnsThem() {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("Experienced speaker")
                .profilePicture("https://example.com/avatar.jpg")
                .build();

        var savedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/avatar.jpg")
                .bio("Experienced speaker")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.insertSpeaker(any(User.class)))
                .thenReturn(Optional.of(savedUser));

        var result = speakerService.createSpeaker(request);

        assertEquals("Experienced speaker", result.getBio());
        assertEquals("https://example.com/avatar.jpg", result.getProfilePicture());
    }

    @Test
    @DisplayName("with external link missing name throws UnprocessableEntityException")
    void withExternalLinkMissingName_throwsUnprocessable() {
        var links = List.of(new ExternalLinkDto(null, "https://example.com"));
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .externalLinks(links)
                .build();

        var savedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.insertSpeaker(any(User.class)))
                .thenReturn(Optional.of(savedUser));

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(externalLinkRepository);
    }

    @Test
    @DisplayName("with external link missing url throws UnprocessableEntityException")
    void withExternalLinkMissingUrl_throwsUnprocessable() {
        var links = List.of(new ExternalLinkDto("Twitter", null));
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .externalLinks(links)
                .build();

        var savedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.insertSpeaker(any(User.class)))
                .thenReturn(Optional.of(savedUser));

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(externalLinkRepository);
    }

    @Test
    @DisplayName("with bio exceeding 1000 characters throws UnprocessableEntityException")
    void withBioTooLong_throwsUnprocessable() {
        var request = SpeakerInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("x".repeat(1001))
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.createSpeaker(request));
        verifyNoInteractions(userRepository);
    }
}

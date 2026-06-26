package com.techindna.eventsyncapi.service.speakers;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
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
import com.techindna.eventsyncapi.validator.ExternalLinkValidator;
import com.techindna.eventsyncapi.validator.SpeakerValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PutSpeakerExternalLinkServiceTest {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final ExternalLinkMapper externalLinkMapper;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String URL_NAME = "Twitter";

    PutSpeakerExternalLinkServiceTest() {
        userRepository = mock(UserRepository.class);
        externalLinkRepository = mock(ExternalLinkRepository.class);
        externalLinkMapper = mock(ExternalLinkMapper.class);
        speakerService = new SpeakerService(
                userRepository, externalLinkRepository, new ExternalLinkValidator(new DataValidator()),
                new SpeakerMapper(new ExternalLinkMapper()), externalLinkMapper,
                mock(SessionRepository.class), mock(SessionMapper.class), mock(AuthService.class),
                new SpeakerValidator(new DataValidator())
        );
    }

    @Test
    @DisplayName("with valid input updates external link and returns all links")
    void withValidInput_updatesExternalLink() {
        var request = new ExternalLinkDto("Twitter", "https://twitter.com/newhandle");

        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        var updatedLink = ExternalLink.builder()
                .id(UUID.randomUUID())
                .name("Twitter")
                .url("https://twitter.com/newhandle")
                .user(speaker)
                .build();

        var allLinks = List.of(
                updatedLink,
                ExternalLink.builder()
                        .id(UUID.randomUUID())
                        .name("GitHub")
                        .url("https://github.com/john")
                        .user(speaker)
                        .build()
        );

        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(externalLinkRepository.updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Twitter"), eq("Twitter"), eq("https://twitter.com/newhandle")
        )).thenReturn(Optional.of(updatedLink));
        when(externalLinkRepository.findByUserId(SPEAKER_ID)).thenReturn(allLinks);
        when(externalLinkMapper.toDto(any(ExternalLink.class)))
                .thenAnswer(invocation -> {
                    var link = invocation.getArgument(0, ExternalLink.class);
                    return new ExternalLinkDto(link.getName(), link.getUrl());
                });

        var result = speakerService.updateExternalLink(SPEAKER_ID, URL_NAME, request);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals("Twitter", result.get(0).getName());
        assertEquals("https://twitter.com/newhandle", result.get(0).getUrl());
        assertEquals("GitHub", result.get(1).getName());

        verify(externalLinkRepository).updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Twitter"), eq("Twitter"), eq("https://twitter.com/newhandle"));
        verify(externalLinkRepository).findByUserId(SPEAKER_ID);
    }

    @Test
    @DisplayName("with unknown speaker throws NotFoundException")
    void withUnknownSpeaker_throwsNotFound() {
        var request = new ExternalLinkDto("Twitter", "https://twitter.com/newhandle");

        when(externalLinkRepository.updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Twitter"), eq("Twitter"), eq("https://twitter.com/newhandle")
        )).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> speakerService.updateExternalLink(SPEAKER_ID, URL_NAME, request));

        assertEquals(
                String.format("Speaker %s or external link 'Twitter' not found.", SPEAKER_ID),
                exception.getMessage()
        );
        verify(externalLinkRepository).updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Twitter"), eq("Twitter"), eq("https://twitter.com/newhandle"));
        verifyNoMoreInteractions(externalLinkRepository);
    }

    @Test
    @DisplayName("with unknown urlName throws NotFoundException")
    void withUnknownUrlName_throwsNotFound() {
        var request = new ExternalLinkDto("Unknown", "https://example.com");

        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        when(externalLinkRepository.updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Unknown"), eq("Unknown"), eq("https://example.com")
        )).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> speakerService.updateExternalLink(SPEAKER_ID, "Unknown", request));

        assertEquals("Speaker " + SPEAKER_ID + " or external link 'Unknown' not found.", exception.getMessage());
        verify(externalLinkRepository).updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Unknown"), eq("Unknown"), eq("https://example.com"));
    }

    @Test
    @DisplayName("with duplicate url throws ConflictException")
    void withDuplicateUrl_throwsConflict() {
        var request = new ExternalLinkDto("Twitter", "https://existing.com/url");

        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .createdAt(LocalDateTime.now())
                .build();

        when(userRepository.findById(SPEAKER_ID)).thenReturn(Optional.of(speaker));
        var sqlEx = new SQLException("", "23505");
        var dataIntegrityEx = new DataIntegrityViolationException("URL already exists.", sqlEx);
        when(externalLinkRepository.updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Twitter"), eq("Twitter"), eq("https://existing.com/url")
        )).thenThrow(dataIntegrityEx);

        var exception = assertThrows(ConflictException.class,
                () -> speakerService.updateExternalLink(SPEAKER_ID, URL_NAME, request));

        assertEquals("URL https://existing.com/url already exists.", exception.getMessage());
        verify(externalLinkRepository).updateExternalLinkByNameAndUserId(
                eq(SPEAKER_ID), eq("Twitter"), eq("Twitter"), eq("https://existing.com/url"));
    }

    @Test
    @DisplayName("with null name throws UnprocessableEntityException")
    void withNullName_throwsUnprocessable() {
        var request = new ExternalLinkDto(null, "https://example.com");

        assertThrows(UnprocessableEntityException.class,
                () -> speakerService.updateExternalLink(SPEAKER_ID, URL_NAME, request));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(externalLinkRepository);
    }

    @Test
    @DisplayName("with null url throws UnprocessableEntityException")
    void withNullUrl_throwsUnprocessable() {
        var request = new ExternalLinkDto("Twitter", null);

        assertThrows(UnprocessableEntityException.class,
                () -> speakerService.updateExternalLink(SPEAKER_ID, URL_NAME, request));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(externalLinkRepository);
    }

    @Test
    @DisplayName("with empty name throws UnprocessableEntityException")
    void withEmptyName_throwsUnprocessable() {
        var request = new ExternalLinkDto("", "https://example.com");

        assertThrows(UnprocessableEntityException.class,
                () -> speakerService.updateExternalLink(SPEAKER_ID, URL_NAME, request));
        verifyNoInteractions(userRepository);
        verifyNoInteractions(externalLinkRepository);
    }
}

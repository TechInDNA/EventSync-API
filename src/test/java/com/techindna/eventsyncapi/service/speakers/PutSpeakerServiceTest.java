package com.techindna.eventsyncapi.service.speakers;

import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateInputDto;
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
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

class PutSpeakerServiceTest {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final AuthService authService;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PutSpeakerServiceTest() {
        userRepository = mock(UserRepository.class);
        externalLinkRepository = mock(ExternalLinkRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        authService = mock(AuthService.class);
        speakerService = new SpeakerService(
                userRepository, externalLinkRepository, new ExternalLinkValidator(new DataValidator()),
                new SpeakerMapper(new ExternalLinkMapper()), mock(ExternalLinkMapper.class),
                sessionRepository, sessionMapper, authService,
                new SpeakerValidator(new DataValidator())
        );
    }

    @Test
    @DisplayName("updateSpeaker with existing id and valid data returns updated speaker")
    void updateSpeaker_withValidData_returnsUpdatedSpeaker() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john.updated@example.com")
                .profilePicture("https://example.com/new-avatar.jpg")
                .bio("Updated bio")
                .build();

        var updatedUser = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john.updated@example.com")
                .profilePicture("https://example.com/new-avatar.jpg")
                .bio("Updated bio")
                .role(Role.SPEAKER)
                .build();

        when(userRepository.updateSpeakerById(eq(SPEAKER_ID), eq("John"), eq("Doe"), eq("john.updated@example.com"),
                eq("https://example.com/new-avatar.jpg"), eq("Updated bio")))
                .thenReturn(Optional.of(updatedUser));

        var result = speakerService.updateSpeaker(SPEAKER_ID, request);

        assertNotNull(result);
        assertEquals(SPEAKER_ID, result.getId());
        assertEquals("John", result.getFirstName());
        assertEquals("Doe", result.getLastName());
        assertEquals("john.updated@example.com", result.getEmail());
        assertEquals("https://example.com/new-avatar.jpg", result.getProfilePicture());
        assertEquals("Updated bio", result.getBio());

        verify(userRepository).updateSpeakerById(eq(SPEAKER_ID), eq("John"), eq("Doe"), eq("john.updated@example.com"),
                eq("https://example.com/new-avatar.jpg"), eq("Updated bio"));
    }

    @Test
    @DisplayName("updateSpeaker with unknown id throws NotFoundException")
    void updateSpeaker_withUnknownId_throwsNotFound() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("https://example.com/pic.jpg")
                .bio("A bio")
                .build();

        when(userRepository.updateSpeakerById(any(), any(), any(), any(), any(), any()))
                .thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        assertEquals("Speaker " + SPEAKER_ID + " not found.", exception.getMessage());

        verify(userRepository).updateSpeakerById(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("updateSpeaker with duplicate email throws ConflictException")
    void updateSpeaker_withDuplicateEmail_throwsConflict() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("existing@example.com")
                .profilePicture("https://example.com/pic.jpg")
                .bio("A bio")
                .build();

        when(userRepository.updateSpeakerById(any(), any(), any(), any(), any(), any()))
                .thenThrow(new DataIntegrityViolationException("duplicate key", new SQLException("", "23505", 23505)));

        var exception = assertThrows(ConflictException.class,
                () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        assertEquals("Email 'existing@example.com' already exists.", exception.getMessage());
    }

    @Test
    @DisplayName("updateSpeaker with null firstName throws UnprocessableEntityException")
    void updateSpeaker_withNullFirstName_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName(null)
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateSpeaker with empty firstName throws UnprocessableEntityException")
    void updateSpeaker_withEmptyFirstName_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("")
                .lastName("Doe")
                .email("john@example.com")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateSpeaker with null lastName throws UnprocessableEntityException")
    void updateSpeaker_withNullLastName_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName(null)
                .email("john@example.com")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateSpeaker with invalid email throws UnprocessableEntityException")
    void updateSpeaker_withInvalidEmail_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("not-an-email")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateSpeaker with invalid profilePicture URL throws UnprocessableEntityException")
    void updateSpeaker_withInvalidProfilePicture_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .profilePicture("not-a-url")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateSpeaker with bio exceeding 1000 characters throws UnprocessableEntityException")
    void updateSpeaker_withBioTooLong_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("x".repeat(1001))
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }

    @Test
    @DisplayName("updateSpeaker with invalid bio characters throws UnprocessableEntityException")
    void updateSpeaker_withInvalidBioChars_throwsUnprocessable() {
        var request = SpeakerUpdateInputDto.builder()
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .bio("Hello <world>")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> speakerService.updateSpeaker(SPEAKER_ID, request));
        verifyNoInteractions(userRepository);
    }
}

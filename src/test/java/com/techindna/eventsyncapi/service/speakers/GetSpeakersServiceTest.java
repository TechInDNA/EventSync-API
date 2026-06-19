package com.techindna.eventsyncapi.service.speakers;

import com.techindna.eventsyncapi.dto.speaker.SpeakerListResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
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

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetSpeakersServiceTest {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final AuthService authService;
    private final SpeakerValidator speakerValidator;
    private final SpeakerService speakerService;

    private static final UUID SPEAKER_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String TEST_IP = "127.0.0.1";

    GetSpeakersServiceTest() {
        userRepository = mock(UserRepository.class);
        externalLinkRepository = mock(ExternalLinkRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        authService = mock(AuthService.class);
        speakerValidator = spy(new SpeakerValidator(new DataValidator()));
        speakerService = new SpeakerService(
                userRepository, externalLinkRepository, new ExternalLinkValidator(new DataValidator()),
                new SpeakerMapper(new ExternalLinkMapper()), mock(ExternalLinkMapper.class),
                sessionRepository, sessionMapper, authService,
                speakerValidator
        );
    }

    @Test
    @DisplayName("getAllSpeakers returns mapped list with pagination")
    void getAllSpeakers_withValidPagination_returnsList() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .build();

        var link = ExternalLink.builder()
                .id(UUID.randomUUID())
                .name("Twitter")
                .url("https://twitter.com/john")
                .user(speaker)
                .build();

        when(userRepository.countSpeakersByNameContaining(null)).thenReturn(1L);
        when(userRepository.findSpeakersByNameContaining(null, 10, 0)).thenReturn(List.of(speaker));
        when(externalLinkRepository.findByUserIdIn(List.of(SPEAKER_ID))).thenReturn(List.of(link));

        SpeakerListResponseDto result = speakerService.getAllSpeakers(1, 10, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getSize());
        assertEquals(1, result.getData().size());
        assertEquals(SPEAKER_ID, result.getData().getFirst().getId());
        assertEquals("John", result.getData().getFirst().getFirstName());
        assertEquals("Doe", result.getData().getFirst().getLastName());
        assertEquals("john@example.com", result.getData().getFirst().getEmail());
        assertNotNull(result.getData().getFirst().getExternalLinks());
        assertEquals(1, result.getData().getFirst().getExternalLinks().size());
        assertEquals("Twitter", result.getData().getFirst().getExternalLinks().getFirst().getName());

        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet(null);
        verify(userRepository).countSpeakersByNameContaining(null);
        verify(userRepository).findSpeakersByNameContaining(null, 10, 0);
        verify(externalLinkRepository).findByUserIdIn(List.of(SPEAKER_ID));
    }

    @Test
    @DisplayName("getAllSpeakers with page 2 returns correct offset")
    void getAllSpeakers_withPage2_returnsCorrectOffset() {
        when(userRepository.countSpeakersByNameContaining(null)).thenReturn(5L);
        when(userRepository.findSpeakersByNameContaining(null, 10, 10)).thenReturn(List.of());
        when(externalLinkRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(2, 10, null, TEST_IP);

        assertNotNull(result);
        assertEquals(5, result.getMeta().getTotal());
        assertEquals(2, result.getMeta().getPage());
        assertTrue(result.getData().isEmpty());

        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet(null);
        verify(userRepository).findSpeakersByNameContaining(null, 10, 10);
    }

    @Test
    @DisplayName("getAllSpeakers with page < 1 defaults to 1")
    void getAllSpeakers_withInvalidPage_defaultsTo1() {
        when(userRepository.countSpeakersByNameContaining(null)).thenReturn(0L);
        when(userRepository.findSpeakersByNameContaining(null, 10, 0)).thenReturn(List.of());
        when(externalLinkRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(0, 10, null, TEST_IP);

        assertEquals(1, result.getMeta().getPage());
        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet(null);
        verify(userRepository).findSpeakersByNameContaining(null, 10, 0);
    }

    @Test
    @DisplayName("getAllSpeakers with size < 1 defaults to 10")
    void getAllSpeakers_withInvalidSize_defaultsTo10() {
        when(userRepository.countSpeakersByNameContaining(null)).thenReturn(0L);
        when(userRepository.findSpeakersByNameContaining(null, 10, 0)).thenReturn(List.of());
        when(externalLinkRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(1, 0, null, TEST_IP);

        assertEquals(10, result.getMeta().getSize());
        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet(null);
        verify(userRepository).findSpeakersByNameContaining(null, 10, 0);
    }

    @Test
    @DisplayName("getAllSpeakers with empty database returns empty list")
    void getAllSpeakers_withNoSpeakers_returnsEmptyList() {
        when(userRepository.countSpeakersByNameContaining(null)).thenReturn(0L);
        when(userRepository.findSpeakersByNameContaining(null, 10, 0)).thenReturn(List.of());
        when(externalLinkRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(1, 10, null, TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet(null);
    }

    @Test
    @DisplayName("getAllSpeakers with search filter returns matching speakers")
    void getAllSpeakers_withSearchFilter_returnsMatchingSpeakers() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .build();

        when(userRepository.countSpeakersByNameContaining("John")).thenReturn(1L);
        when(userRepository.findSpeakersByNameContaining("John", 10, 0)).thenReturn(List.of(speaker));
        when(externalLinkRepository.findByUserIdIn(List.of(SPEAKER_ID))).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(1, 10, "John", TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals("John", result.getData().getFirst().getFirstName());
        assertEquals("john@example.com", result.getData().getFirst().getEmail());
        assertEquals(1, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet("John");
        verify(userRepository).countSpeakersByNameContaining("John");
        verify(userRepository).findSpeakersByNameContaining("John", 10, 0);
    }

    @Test
    @DisplayName("getAllSpeakers with search filter and no match returns empty list")
    void getAllSpeakers_withSearchFilter_noMatch_returnsEmpty() {
        when(userRepository.countSpeakersByNameContaining("Nonexistent")).thenReturn(0L);
        when(userRepository.findSpeakersByNameContaining("Nonexistent", 10, 0)).thenReturn(List.of());
        when(externalLinkRepository.findByUserIdIn(List.of())).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(1, 10, "Nonexistent", TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet("Nonexistent");
        verify(userRepository).countSpeakersByNameContaining("Nonexistent");
        verify(userRepository).findSpeakersByNameContaining("Nonexistent", 10, 0);
    }

    @Test
    @DisplayName("getAllSpeakers with speaker having no external links returns speaker without externalLinks")
    void getAllSpeakers_withNoExternalLinks_returnsSpeakerWithoutLinks() {
        var speaker = User.builder()
                .id(SPEAKER_ID)
                .firstName("John")
                .lastName("Doe")
                .email("john@example.com")
                .role(Role.SPEAKER)
                .build();

        when(userRepository.countSpeakersByNameContaining(null)).thenReturn(1L);
        when(userRepository.findSpeakersByNameContaining(null, 10, 0)).thenReturn(List.of(speaker));
        when(externalLinkRepository.findByUserIdIn(List.of(SPEAKER_ID))).thenReturn(List.of());

        SpeakerListResponseDto result = speakerService.getAllSpeakers(1, 10, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertNull(result.getData().getFirst().getExternalLinks());

        verify(authService).checkBlacklist(TEST_IP);
        verify(speakerValidator).validateGet(null);
        verify(userRepository).countSpeakersByNameContaining(null);
        verify(userRepository).findSpeakersByNameContaining(null, 10, 0);
        verify(externalLinkRepository).findByUserIdIn(List.of(SPEAKER_ID));
    }
}

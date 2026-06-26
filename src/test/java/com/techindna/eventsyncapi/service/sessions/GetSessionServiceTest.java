package com.techindna.eventsyncapi.service.sessions;

import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.dto.session.EventRefDto;
import com.techindna.eventsyncapi.dto.session.RoomRefDto;
import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SpeakerRefDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.entity.Question;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.TooManyRequestException;
import com.techindna.eventsyncapi.mapper.*;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.SessionService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.SessionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

class GetSessionServiceTest {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionService sessionService;
    private final AuthService authService;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;

    private static final UUID SESSION_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final UUID EVENT_ID = UUID.fromString("c3d4e5f6-a7b8-9012-cdef-123456789012");
    private static final UUID SPEAKER_ID = UUID.fromString("d4e5f6a7-b8c9-0123-def0-234567890123");
    private static final UUID QUESTION_ID = UUID.fromString("e5f6a7b8-c9d0-1234-ef01-345678901234");
    private static final String SESSION_TITLE = "Keynote Speech";
    private static final String TEST_IP = "127.0.0.1";

    GetSessionServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        authService = mock(AuthService.class);
        questionRepository = mock(QuestionRepository.class);
        questionMapper = mock(QuestionMapper.class);
        sessionService = new SessionService(
                sessionRepository, sessionMapper, new SessionValidator(new DataValidator()),
                authService, questionRepository, questionMapper
        );
    }

    @Test
    @DisplayName("with valid session ID returns session detail with speakers and questions")
    void withValidSessionId_returnsSessionDetail() {
        var room = Room.builder().id(ROOM_ID).name("Salle Principale").build();
        var event = Event.builder().id(EVENT_ID).title("Conference 2025").build();
        var speaker = User.builder().id(SPEAKER_ID).firstName("John").lastName("Doe").build();

        var session = Session.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .description("An inspiring keynote speech")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .room(room)
                .capacity(100)
                .event(event)
                .speakers(List.of(speaker))
                .build();

        var expectedDto = SessionDetailResponseDto.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .description("An inspiring keynote speech")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .room(RoomRefDto.builder().id(ROOM_ID).name("Salle Principale").build())
                .capacity(100)
                .event(EventRefDto.builder().id(EVENT_ID).title("Conference 2025").build())
                .questions(null)
                .build();

        when(sessionRepository.findByIdWithDetails(SESSION_ID)).thenReturn(Optional.of(session));
        when(questionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of());
        when(sessionMapper.toDetailResponseDto(eq(session), isNull())).thenReturn(expectedDto);

        SessionDetailResponseDto result = sessionService.getSessionById(SESSION_ID, TEST_IP);

        assertNotNull(result);
        assertEquals(SESSION_ID, result.getId());
        assertEquals(SESSION_TITLE, result.getTitle());
        assertEquals("An inspiring keynote speech", result.getDescription());
        assertEquals(Instant.parse("2025-06-01T09:00:00Z"), result.getStartDate());
        assertEquals(Instant.parse("2025-06-01T10:00:00Z"), result.getEndDate());
        assertEquals(100, result.getCapacity());

        assertNotNull(result.getRoom());
        assertEquals(ROOM_ID, result.getRoom().getId());
        assertEquals("Salle Principale", result.getRoom().getName());

        assertNotNull(result.getEvent());
        assertEquals(EVENT_ID, result.getEvent().getId());
        assertEquals("Conference 2025", result.getEvent().getTitle());

        assertNull(result.getQuestions());

        verify(authService).checkBlacklist(TEST_IP);
        verify(sessionRepository).findByIdWithDetails(SESSION_ID);
        verify(questionRepository).findBySessionId(SESSION_ID);
    }

    @Test
    @DisplayName("with session ID that does not exist throws NotFoundException")
    void withNonExistentSessionId_throwsNotFound() {
        when(sessionRepository.findByIdWithDetails(SESSION_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> sessionService.getSessionById(SESSION_ID, TEST_IP));

        assertEquals("Session a1b2c3d4-e5f6-7890-abcd-ef1234567890 not found.", exception.getMessage());

        verify(authService).checkBlacklist(TEST_IP);
        verify(sessionRepository).findByIdWithDetails(SESSION_ID);
        verifyNoInteractions(questionRepository);
    }

    @Test
    @DisplayName("with blacklisted IP throws TooManyRequestException")
    void withBlacklistedIp_throwsTooManyRequests() {
        doThrow(new TooManyRequestException("IP blacklisted."))
                .when(authService).checkBlacklist(TEST_IP);

        assertThrows(TooManyRequestException.class,
                () -> sessionService.getSessionById(SESSION_ID, TEST_IP));

        verify(authService).checkBlacklist(TEST_IP);
        verifyNoInteractions(sessionRepository, questionRepository);
    }

    @Test
    @DisplayName("with questions returns session detail with mapped question DTOs")
    void withQuestions_returnsSessionDetailWithQuestions() {
        var room = Room.builder().id(ROOM_ID).name("Salle Principale").build();
        var event = Event.builder().id(EVENT_ID).title("Conference 2025").build();

        var session = Session.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .description("Session with questions")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .room(room)
                .capacity(50)
                .event(event)
                .speakers(List.of())
                .build();

        var question = Question.builder()
                .id(QUESTION_ID)
                .title("Dependency Injection?")
                .content("How does Spring resolve circular dependencies?")
                .createdAt(Instant.parse("2025-06-01T09:05:00Z"))
                .session(session)
                .anonymous(false)
                .user(User.builder()
                        .id(UUID.fromString("ebcfde24-726e-4bb4-8b55-786781d8b6bc"))
                        .firstName("Alice")
                        .lastName("Dupont")
                        .email("alice@example.com")
                        .build())
                .upvoteCount(3)
                .build();

        when(sessionRepository.findByIdWithDetails(SESSION_ID)).thenReturn(Optional.of(session));
        when(questionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(question));

        var userMapper = new UserMapper();
        var questionMapper = new QuestionMapper(userMapper);
        var roomMapper = new RoomMapper();
        var eventMapper = new EventMapper();
        var speakerMapper = new SpeakerMapper(mock(ExternalLinkMapper.class));
        var sessionMapper = new SessionMapper(eventMapper, roomMapper, speakerMapper);

        var serviceWithRealMappers = new SessionService(
                sessionRepository, sessionMapper, new SessionValidator(new DataValidator()),
                authService, questionRepository, questionMapper
        );

        SessionDetailResponseDto result = serviceWithRealMappers.getSessionById(SESSION_ID, TEST_IP);

        assertNotNull(result);
        assertEquals(SESSION_ID, result.getId());
        assertNull(result.getSpeakers());

        assertNotNull(result.getQuestions());
        assertEquals(1, result.getQuestions().size());
        QuestionResponseDto qdto = result.getQuestions().getFirst();
        assertEquals(QUESTION_ID, qdto.getId());
        assertEquals("Dependency Injection?", qdto.getTitle());
        assertEquals("How does Spring resolve circular dependencies?", qdto.getContent());
        assertEquals(3, qdto.getUpvotes());
        assertFalse(qdto.isAnonymous());

        assertNotNull(qdto.getParticipant());
        assertEquals("Alice", qdto.getParticipant().getFirstName());
        assertEquals("Dupont", qdto.getParticipant().getLastName());
        assertEquals("alice@example.com", qdto.getParticipant().getEmail());

        verify(authService).checkBlacklist(TEST_IP);
        verify(sessionRepository).findByIdWithDetails(SESSION_ID);
        verify(questionRepository).findBySessionId(SESSION_ID);
        verifyNoMoreInteractions(questionRepository);
    }

    @Test
    @DisplayName("with anonymous questions hides participant ref")
    void withAnonymousQuestion_hidesParticipantRef() {
        var room = Room.builder().id(ROOM_ID).name("Salle Principale").build();
        var event = Event.builder().id(EVENT_ID).title("Conference 2025").build();

        var session = Session.builder()
                .id(SESSION_ID)
                .title(SESSION_TITLE)
                .description("Session with anonymous question")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-01T10:00:00Z"))
                .room(room)
                .capacity(50)
                .event(event)
                .speakers(List.of())
                .build();

        var question = Question.builder()
                .id(QUESTION_ID)
                .title("Anonymous question")
                .content("This is anonymous")
                .createdAt(Instant.parse("2025-06-01T09:05:00Z"))
                .session(session)
                .anonymous(true)
                .user(User.builder()
                        .id(UUID.fromString("b5718099-653c-4ed5-b8f1-da808ff2dc7e"))
                        .firstName("Charlie")
                        .lastName("Bernard")
                        .email("charlie@example.com")
                        .build())
                .upvoteCount(0)
                .build();

        when(sessionRepository.findByIdWithDetails(SESSION_ID)).thenReturn(Optional.of(session));
        when(questionRepository.findBySessionId(SESSION_ID)).thenReturn(List.of(question));

        var userMapper = new UserMapper();
        var questionMapper = new QuestionMapper(userMapper);
        var roomMapper = new RoomMapper();
        var eventMapper = new EventMapper();
        var speakerMapper = new SpeakerMapper(mock(ExternalLinkMapper.class));
        var sessionMapper = new SessionMapper(eventMapper, roomMapper, speakerMapper);

        var serviceWithRealMappers = new SessionService(
                sessionRepository, sessionMapper, new SessionValidator(new DataValidator()),
                authService, questionRepository, questionMapper
        );

        SessionDetailResponseDto result = serviceWithRealMappers.getSessionById(SESSION_ID, TEST_IP);

        assertNotNull(result);
        assertNotNull(result.getQuestions());
        assertEquals(1, result.getQuestions().size());
        QuestionResponseDto qdto = result.getQuestions().getFirst();
        assertTrue(qdto.isAnonymous());
        assertNull(qdto.getParticipant());
        assertEquals(0, qdto.getUpvotes());

        verify(authService).checkBlacklist(TEST_IP);
        verify(sessionRepository).findByIdWithDetails(SESSION_ID);
        verify(questionRepository).findBySessionId(SESSION_ID);
        verifyNoMoreInteractions(questionRepository);
    }
}

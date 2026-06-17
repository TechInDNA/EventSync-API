package com.techindna.eventsyncapi.service.question;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.entity.Question;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.QuestionMapper;
import com.techindna.eventsyncapi.mapper.UserMapper;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.QuestionService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetQuestionServiceTest {

    private final SessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final AuthService authService;
    private final DataValidator dataValidator;
    private final QuestionService questionService;

    private static final UUID SESSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID QUESTION_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final String TEST_IP = "127.0.0.1";
    private static final String SEARCH_QUERY = "Spring";
    private static final String SORT_FIELD = "creationDate";

    GetQuestionServiceTest() {
        sessionRepository = mock(SessionRepository.class);
        questionRepository = mock(QuestionRepository.class);

        UserMapper userMapper = mock(UserMapper.class);
        questionMapper = new QuestionMapper(userMapper);

        authService = mock(AuthService.class);
        dataValidator = mock(DataValidator.class);

        questionService = new QuestionService(
                sessionRepository,
                questionRepository,
                questionMapper,
                authService,
                dataValidator
        );
    }

    @Test
    @DisplayName("getQuestionsBySessionId returns mapped questions with valid pagination")
    void getQuestions_withValidPagination_returnsList() {

        var mockSession = Session.builder().id(SESSION_ID).build();
        var question = Question.builder()
                .id(QUESTION_ID)
                .title("How does Spring work?")
                .content("Can someone explain DI?")
                .session(mockSession)
                .anonymous(true)
                .createdAt(Instant.parse("2026-06-17T00:00:00Z"))
                .build();

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(mockSession));
        when(questionRepository.countBySessionId(SESSION_ID)).thenReturn(1L);
        when(questionRepository.findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 10, 0))
                .thenReturn(List.of(question));

        QuestionListResponseDto result = questionService.getQuestionsBySessionId(
                SESSION_ID, 1, 10, SORT_FIELD, SEARCH_QUERY, TEST_IP
        );

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getSize());
        assertEquals(1, result.getData().size());
        assertEquals(QUESTION_ID, result.getData().getFirst().getId());
        assertEquals("How does Spring work?", result.getData().getFirst().getTitle());
        assertTrue(result.getData().getFirst().isAnonymous());


        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator).validateSearchString(SEARCH_QUERY);
        verify(sessionRepository).findById(SESSION_ID);
        verify(questionRepository).countBySessionId(SESSION_ID);
        verify(questionRepository).findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 10, 0);
    }

    @Test
    @DisplayName("getQuestionsBySessionId with page 3 returns correct offset calculation")
    void getQuestions_withPage3_returnsCorrectOffset() {
        // Given
        var mockSession = Session.builder().id(SESSION_ID).build();
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(mockSession));
        when(questionRepository.countBySessionId(SESSION_ID)).thenReturn(25L);
        when(questionRepository.findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 5, 10))
                .thenReturn(List.of());


        QuestionListResponseDto result = questionService.getQuestionsBySessionId(
                SESSION_ID, 3, 5, SORT_FIELD, SEARCH_QUERY, TEST_IP
        );


        assertNotNull(result);
        assertEquals(25, result.getMeta().getTotal());
        assertEquals(3, result.getMeta().getPage());
        assertEquals(5, result.getMeta().getSize());
        assertTrue(result.getData().isEmpty());


        verify(questionRepository).findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 5, 10);
    }

    @Test
    @DisplayName("getQuestionsBySessionId when page < 1 defaults page to 1 and offset to 0")
    void getQuestions_withInvalidPage_defaultsToPage1() {

        var mockSession = Session.builder().id(SESSION_ID).build();
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(mockSession));
        when(questionRepository.countBySessionId(SESSION_ID)).thenReturn(0L);
        when(questionRepository.findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 10, 0))
                .thenReturn(List.of());


        QuestionListResponseDto result = questionService.getQuestionsBySessionId(
                SESSION_ID, 0, 10, SORT_FIELD, SEARCH_QUERY, TEST_IP
        );


        assertEquals(1, result.getMeta().getPage());
        verify(questionRepository).findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 10, 0);
    }

    @Test
    @DisplayName("getQuestionsBySessionId when size < 1 defaults size to 10")
    void getQuestions_withInvalidSize_defaultsToSize10() {

        var mockSession = Session.builder().id(SESSION_ID).build();
        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.of(mockSession));
        when(questionRepository.countBySessionId(SESSION_ID)).thenReturn(0L);
        when(questionRepository.findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 10, 0))
                .thenReturn(List.of());


        QuestionListResponseDto result = questionService.getQuestionsBySessionId(
                SESSION_ID, 1, -5, SORT_FIELD, SEARCH_QUERY, TEST_IP
        );


        assertEquals(10, result.getMeta().getSize());
        verify(questionRepository).findBySessionIdWithPagination(SESSION_ID, SORT_FIELD, 10, 0);
    }

    @Test
    @DisplayName("getQuestionsBySessionId throws NotFoundException when session does not exist")
    void getQuestions_sessionNotFound_throwsNotFoundException() {

        when(sessionRepository.findById(SESSION_ID)).thenReturn(Optional.empty());


        NotFoundException exception = assertThrows(NotFoundException.class, () ->
                questionService.getQuestionsBySessionId(SESSION_ID, 1, 10, SORT_FIELD, SEARCH_QUERY, TEST_IP)
        );

        assertEquals(String.format("Session %s not found.", SESSION_ID), exception.getMessage());


        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator).validateSearchString(SEARCH_QUERY);
        verify(sessionRepository).findById(SESSION_ID);
        verifyNoInteractions(questionRepository);
    }
}
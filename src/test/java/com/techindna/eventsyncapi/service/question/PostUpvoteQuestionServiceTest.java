package com.techindna.eventsyncapi.service.question;

import com.techindna.eventsyncapi.dto.question.UpvoteResponseDto;
import com.techindna.eventsyncapi.entity.Question;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.QuestionMapper;
import com.techindna.eventsyncapi.mapper.UserMapper;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.QuestionService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.QuestionValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class PostUpvoteQuestionServiceTest {

    private final QuestionRepository questionRepository;
    private final QuestionService questionService;

    private static final UUID QUESTION_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final UUID SESSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");
    private static final UUID OTHER_USER_ID = UUID.fromString("ffffffff-1111-2222-3333-444444444444");

    PostUpvoteQuestionServiceTest() {
        questionRepository = mock(QuestionRepository.class);
        var sessionRepository = mock(SessionRepository.class);
        var userMapper = mock(UserMapper.class);
        var questionMapper = new QuestionMapper(userMapper);
        var authService = mock(AuthService.class);
        var dataValidator = mock(DataValidator.class);
        var questionValidator = mock(QuestionValidator.class);

        questionService = new QuestionService(
                sessionRepository,
                questionRepository,
                questionMapper,
                authService,
                dataValidator,
                questionValidator
        );
    }

    @Test
    @DisplayName("upvoteQuestion deletes existing upvote, inserts new one, returns updated count")
    void upvoteQuestion_firstUpvote_addsAndReturnsCount1() {
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(Question.builder().id(QUESTION_ID).build()));
        when(questionRepository.deleteUpvote(USER_ID, QUESTION_ID)).thenReturn(0);
        when(questionRepository.insertUpvote(USER_ID, QUESTION_ID)).thenReturn(1);
        when(questionRepository.countUpvotesByQuestionId(QUESTION_ID)).thenReturn(1);

        UpvoteResponseDto result = questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID);

        assertNotNull(result);
        assertEquals(1, result.getUpvoteCount());

        verify(questionRepository).findById(QUESTION_ID);
        verify(questionRepository).deleteUpvote(USER_ID, QUESTION_ID);
        verify(questionRepository).insertUpvote(USER_ID, QUESTION_ID);
        verify(questionRepository).countUpvotesByQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("upvoteQuestion when user already upvoted toggles off and returns updated count")
    void upvoteQuestion_reUpvote_togglesOffAndReturnsUpdatedCount() {
        var question = Question.builder().id(QUESTION_ID).build();
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
        when(questionRepository.deleteUpvote(USER_ID, QUESTION_ID)).thenReturn(1);
        when(questionRepository.countUpvotesByQuestionId(QUESTION_ID)).thenReturn(2);

        UpvoteResponseDto result = questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID);

        assertNotNull(result);
        assertEquals(2, result.getUpvoteCount());

        verify(questionRepository).findById(QUESTION_ID);
        verify(questionRepository).deleteUpvote(USER_ID, QUESTION_ID);
        verify(questionRepository, never()).insertUpvote(any(), any());
        verify(questionRepository).countUpvotesByQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("upvoteQuestion called by two different users both succeed")
    void upvoteQuestion_twoUsers_bothSucceed() {
        var question = Question.builder().id(QUESTION_ID).build();
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
        when(questionRepository.deleteUpvote(USER_ID, QUESTION_ID)).thenReturn(0);
        when(questionRepository.deleteUpvote(OTHER_USER_ID, QUESTION_ID)).thenReturn(0);
        when(questionRepository.insertUpvote(any(), eq(QUESTION_ID))).thenReturn(1);
        when(questionRepository.countUpvotesByQuestionId(QUESTION_ID)).thenReturn(2);

        questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID);
        questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, OTHER_USER_ID);

        verify(questionRepository, times(2)).findById(QUESTION_ID);
        verify(questionRepository).insertUpvote(USER_ID, QUESTION_ID);
        verify(questionRepository).insertUpvote(OTHER_USER_ID, QUESTION_ID);
        verify(questionRepository, times(2)).countUpvotesByQuestionId(QUESTION_ID);
    }

    @Test
    @DisplayName("upvoteQuestion when question not found throws NotFoundException")
    void upvoteQuestion_questionNotFound_throwsNotFoundException() {
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class,
                () -> questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID));

        assertEquals(
                String.format("Question %s not found.", QUESTION_ID),
                exception.getMessage()
        );

        verify(questionRepository).findById(QUESTION_ID);
        verify(questionRepository, never()).deleteUpvote(any(), any());
        verify(questionRepository, never()).insertUpvote(any(), any());
        verify(questionRepository, never()).countUpvotesByQuestionId(any());
    }

    @Test
    @DisplayName("upvoteQuestion returns accurate count from repository")
    void upvoteQuestion_returnsAccurateCount() {
        var question = Question.builder().id(QUESTION_ID).build();
        when(questionRepository.findById(QUESTION_ID)).thenReturn(Optional.of(question));
        when(questionRepository.deleteUpvote(USER_ID, QUESTION_ID)).thenReturn(0);
        when(questionRepository.insertUpvote(USER_ID, QUESTION_ID)).thenReturn(1);
        when(questionRepository.countUpvotesByQuestionId(QUESTION_ID)).thenReturn(42);

        UpvoteResponseDto result = questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID);

        assertEquals(42, result.getUpvoteCount());
        verify(questionRepository).countUpvotesByQuestionId(QUESTION_ID);
    }
}

package com.techindna.eventsyncapi.controller.question;

import com.techindna.eventsyncapi.controller.QuestionController;
import com.techindna.eventsyncapi.dto.question.UpvoteResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class PostUpvoteQuestionControllerTest {

    private final MockMvc mockMvc;
    private final QuestionService questionService;

    private static final UUID SESSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID QUESTION_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final UUID USER_ID = UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeeeeeeeee");

    PostUpvoteQuestionControllerTest() {
        questionService = mock(QuestionService.class);
        var controller = new QuestionController(questionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    private static RequestPostProcessor withAuthentication(UUID userId) {
        return request -> {
            request.setUserPrincipal(
                    new UsernamePasswordAuthenticationToken(userId.toString(), null)
            );
            return request;
        };
    }

    @Test
    @DisplayName("POST /sessions/{id}/questions/{qid}/upvote returns 200 with upvoteCount")
    void upvoteQuestion_withAuth_returns200() throws Exception {
        var response = UpvoteResponseDto.builder()
                .upvoteCount(5)
                .build();

        when(questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID)).thenReturn(response);

        mockMvc.perform(post("/sessions/{id}/questions/{qid}/upvote", SESSION_ID, QUESTION_ID)
                        .with(withAuthentication(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvoteCount").value(5));
    }

    @Test
    @DisplayName("POST /sessions/{id}/questions/{qid}/upvote returns 200 with zero upvotes")
    void upvoteQuestion_withZeroUpvotes_returns200() throws Exception {
        var response = UpvoteResponseDto.builder()
                .upvoteCount(0)
                .build();

        when(questionService.upvoteQuestion(QUESTION_ID, SESSION_ID, USER_ID)).thenReturn(response);

        mockMvc.perform(post("/sessions/{id}/questions/{qid}/upvote", SESSION_ID, QUESTION_ID)
                        .with(withAuthentication(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.upvoteCount").value(0));
    }

    @Test
    @DisplayName("POST /sessions/{id}/questions/{qid}/upvote when question not found returns 404")
    void upvoteQuestion_questionNotFound_returns404() throws Exception {
        when(questionService.upvoteQuestion(any(), any(), any()))
                .thenThrow(new NotFoundException("Question " + QUESTION_ID + " not found."));

        mockMvc.perform(post("/sessions/{id}/questions/{qid}/upvote", SESSION_ID, QUESTION_ID)
                        .with(withAuthentication(USER_ID))
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Question " + QUESTION_ID + " not found."));
    }
}

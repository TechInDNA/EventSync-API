package com.techindna.eventsyncapi.controller.question;

import com.techindna.eventsyncapi.controller.QuestionController;
import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.TooManyRequestException;
import com.techindna.eventsyncapi.service.QuestionService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetQuestionControllerTest {

    private final MockMvc mockMvc;
    private final QuestionService questionService;

    private static final UUID SESSION_ID = UUID.fromString("11111111-2222-3333-4444-555555555555");
    private static final UUID QUESTION_ID = UUID.fromString("99999999-8888-7777-6666-555555555555");
    private static final String DEFAULT_SORT = "upvotes";
    private static final String MOCK_IP = "127.0.0.1";

    GetQuestionControllerTest() {
        questionService = mock(QuestionService.class);
        var controller = new QuestionController(questionService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions returns 200 with default pagination and sorting")
    void getQuestions_withDefaultParameters_returns200AndList() throws Exception {

        var questions = List.of(
                QuestionResponseDto.builder()
                        .id(QUESTION_ID)
                        .title("How does Spring work?")
                        .content("Can someone explain Dependency Injection?")
                        .anonymous(true)
                        .upvotes(0)
                        .createdAt(Instant.now())
                        .build()
        );
        var response = QuestionListResponseDto.builder()
                .data(questions)
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), any(), nullable(String.class), any()))
                .thenReturn(response);


        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(QUESTION_ID.toString()))
                .andExpect(jsonPath("$.data[0].title").value("How does Spring work?"))
                .andExpect(jsonPath("$.data[0].isAnonymous").value(true))
                .andExpect(jsonPath("$.meta.total").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(20));

        verify(questionService).getQuestionsBySessionId(
                eq(SESSION_ID), eq(1), eq(20), eq(DEFAULT_SORT), isNull(), any()
        );
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions with custom params returns 200")
    void getQuestions_withCustomParameters_returns200() throws Exception {

        var response = QuestionListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(2).size(20).build())
                .build();

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), any(), nullable(String.class), any()))
                .thenReturn(response);


        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .param("page", "2")
                        .param("size", "20")
                        .param("sort", "createdAt")
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.size").value(20));

        verify(questionService).getQuestionsBySessionId(
                eq(SESSION_ID), eq(2), eq(20), eq("createdAt"), isNull(), any()
        );
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions when no questions found returns 200 with empty list")
    void getQuestions_whenEmpty_returns200WithEmptyData() throws Exception {

        var response = QuestionListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(20).build())
                .build();

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), any(), nullable(String.class), any()))
                .thenReturn(response);

        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.meta.total").value(0));

        verify(questionService).getQuestionsBySessionId(
                eq(SESSION_ID), eq(1), eq(20), eq(DEFAULT_SORT), isNull(), any()
        );
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions sorted by upvotes returns 200")
    void getQuestions_withUpvotesSort_returns200() throws Exception {

        var questions = List.of(
                QuestionResponseDto.builder()
                        .id(QUESTION_ID)
                        .title("Top question")
                        .content("Most upvoted")
                        .anonymous(false)
                        .upvotes(42)
                        .createdAt(Instant.now())
                        .build()
        );
        var response = QuestionListResponseDto.builder()
                .data(questions)
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), eq("upvotes"), nullable(String.class), any()))
                .thenReturn(response);

        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .param("sort", "upvotes")
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].upvotes").value(42))
                .andExpect(jsonPath("$.meta.total").value(1));

        verify(questionService).getQuestionsBySessionId(
                eq(SESSION_ID), eq(1), eq(20), eq("upvotes"), isNull(), any()
        );
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions when session not found returns 404")
    void getQuestions_whenSessionNotFound_returns404() throws Exception {

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), any(), nullable(String.class), any()))
                .thenThrow(new NotFoundException("Session " + SESSION_ID + " not found."));

        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Session " + SESSION_ID + " not found."));
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions when IP is blacklisted returns 429")
    void getQuestions_whenBlacklistedIp_returns429() throws Exception {

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), any(), nullable(String.class), any()))
                .thenThrow(new TooManyRequestException("Too many requests. Please try again later."));

        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.status").value(429))
                .andExpect(jsonPath("$.error").value("Too Many Requests"))
                .andExpect(jsonPath("$.message").value("Too many requests. Please try again later."));
    }

    @Test
    @DisplayName("GET /sessions/{id}/questions with title filter passes title to service")
    void getQuestions_withTitleFilter_passesTitleToService() throws Exception {
        var questions = List.of(
                QuestionResponseDto.builder()
                        .id(QUESTION_ID)
                        .title("How does Spring work?")
                        .content("DI explanation")
                        .anonymous(true)
                        .upvotes(3)
                        .createdAt(Instant.now())
                        .build()
        );
        var response = QuestionListResponseDto.builder()
                .data(questions)
                .meta(MetaDto.builder().total(1).page(1).size(20).build())
                .build();

        when(questionService.getQuestionsBySessionId(eq(SESSION_ID), anyInt(), anyInt(), any(), nullable(String.class), any()))
                .thenReturn(response);

        mockMvc.perform(get("/sessions/{id}/questions", SESSION_ID)
                        .param("title", "Spring")
                        .with(request -> { request.setRemoteAddr(MOCK_IP); return request; })
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].title").value("How does Spring work?"))
                .andExpect(jsonPath("$.meta.total").value(1));

        verify(questionService).getQuestionsBySessionId(
                eq(SESSION_ID), eq(1), eq(20), eq(DEFAULT_SORT), eq("Spring"), any()
        );
    }
}

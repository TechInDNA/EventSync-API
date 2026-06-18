package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.dto.question.QuestionRequestDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class QuestionController {
    private final QuestionService questionService;

    @GetMapping("/{id}/questions")
    public ResponseEntity<QuestionListResponseDto> getQuestionsBySessionId(
            @PathVariable UUID id,
            @RequestParam(defaultValue = "upvotes") String sort,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String title,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(questionService.getQuestionsBySessionId(
                        id,
                        page,
                        size,
                        sort,
                        title,
                        request.getRemoteAddr()
                ));
    }

    @PostMapping("/{id}/questions")
    public ResponseEntity<QuestionResponseDto> createQuestion(
            @PathVariable UUID id,
            @RequestBody QuestionRequestDto request,
            HttpServletRequest httpRequest
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(
                        id,
                        request,
                        httpRequest.getRemoteAddr()
                ));
    }
}

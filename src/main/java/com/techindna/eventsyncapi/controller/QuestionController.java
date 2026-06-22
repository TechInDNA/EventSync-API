package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.dto.question.QuestionRequestDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.exception.BadRequestException;
import com.techindna.eventsyncapi.exception.InternalServerErrorException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.QuestionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
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
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(questionService.createQuestion(id, request, userId));
    }

    @GetMapping("/{id}/questions/{qid}/upvote")
    public ResponseEntity<?> getUpvoteCount(@PathVariable String id, @PathVariable String qid) {
        try {
            return ResponseEntity.status(HttpStatus.OK)
                    .body(questionService.getUpvoteCount(id, qid));
        } catch (BadRequestException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(e.getMessage());
        } catch (NotFoundException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(e.getMessage());
        } catch (InternalServerErrorException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("An unexpected error occurred, please try again later");
        }
    }
}

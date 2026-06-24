package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.ai.AiConversationDetailResponseDto;
import com.techindna.eventsyncapi.dto.ai.AiConversationListResponseDto;
import com.techindna.eventsyncapi.dto.ai.ChatMessageInputDto;
import com.techindna.eventsyncapi.dto.ai.ChatMessageResponseDto;
import com.techindna.eventsyncapi.service.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ai/conversations")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService aiConversationService;

    @PostMapping
    public ResponseEntity<ChatMessageResponseDto> createConversation(
            @RequestBody ChatMessageInputDto request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aiConversationService.createConversation(userId, request));
    }

    @GetMapping
    public ResponseEntity<AiConversationListResponseDto> getConversations(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            @RequestParam(required = false) String search,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(aiConversationService.getConversations(userId, page, size, search));
    }

    @PostMapping("/{id}")
    public ResponseEntity<ChatMessageResponseDto> continueConversation(
            @PathVariable UUID id,
            @RequestBody ChatMessageInputDto request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aiConversationService.continueConversation(id, userId, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteConversation(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        aiConversationService.deleteConversation(id, UUID.fromString(authentication.getName()));
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<AiConversationDetailResponseDto> getConversation(
            @PathVariable UUID id,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.OK)
                .body(aiConversationService.getConversation(id, userId));
    }
}

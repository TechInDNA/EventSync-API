package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.ai.AiConversationInputDto;
import com.techindna.eventsyncapi.dto.ai.AiConversationResponseDto;
import com.techindna.eventsyncapi.service.AiConversationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/ai/conversations")
@RequiredArgsConstructor
public class AiConversationController {

    private final AiConversationService aiConversationService;

    @PostMapping
    public ResponseEntity<AiConversationResponseDto> createConversation(
            @RequestBody AiConversationInputDto request,
            Authentication authentication
    ) {
        UUID userId = UUID.fromString(authentication.getName());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(aiConversationService.createConversation(userId, request));
    }
}

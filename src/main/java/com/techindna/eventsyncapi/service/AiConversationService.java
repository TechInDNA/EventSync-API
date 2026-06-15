package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.ai.AiConversationInputDto;
import com.techindna.eventsyncapi.dto.ai.AiConversationResponseDto;
import com.techindna.eventsyncapi.mapper.AiConversationMapper;
import com.techindna.eventsyncapi.repository.AiConversationRepository;
import com.techindna.eventsyncapi.validator.AiConversationsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiConversationService {

    private final AiConversationRepository aiConversationRepository;
    private final AiConversationsValidator aiConversationsValidator;
    private final AiConversationMapper aiConversationMapper;
    private final AiApiService aiApiService;

    @Transactional
    public AiConversationResponseDto createConversation(UUID userId, AiConversationInputDto request) {
        String userRequest = aiConversationsValidator.validateUserRequest(request.getUserRequest());

        String aiResponse = aiApiService.sendMessage(userRequest);
        String title = aiApiService.generateTitle(userRequest);

        return aiConversationMapper.toResponseDto(
                aiConversationRepository.insertConversation(
                        title, userRequest, aiResponse, userId
                ).orElseThrow(() -> new RuntimeException("Failed to save AI conversation."))
        );
    }
}

package com.techindna.eventsyncapi.service;

import com.openai.errors.InternalServerException;
import com.techindna.eventsyncapi.dto.ai.ChatMessageInputDto;
import com.techindna.eventsyncapi.dto.ai.ChatMessageResponseDto;
import com.techindna.eventsyncapi.entity.AiConversation;
import com.techindna.eventsyncapi.entity.ChatMessage;
import com.techindna.eventsyncapi.exception.InternalServerErrorException;
import com.techindna.eventsyncapi.mapper.ChatMessageMapper;
import com.techindna.eventsyncapi.repository.AiConversationRepository;
import com.techindna.eventsyncapi.repository.ChatMessageRepository;
import com.techindna.eventsyncapi.validator.AiConversationsValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AiConversationService {

    private final AiConversationRepository aiConversationRepository;
    private final ChatMessageRepository chatMessageRepository;
    private final AiConversationsValidator aiConversationsValidator;
    private final ChatMessageMapper chatMessageMapper;
    private final AiApiService aiApiService;

    @Transactional
    public ChatMessageResponseDto createConversation(UUID userId, ChatMessageInputDto request) {
        String content = aiConversationsValidator.validateUserRequest(request.getContent());

        String title = aiApiService.generateTitle(content)
                .orElseThrow(() -> new InternalServerErrorException("An error occurred during Title generation."));

        AiConversation conversation = aiConversationRepository.insertConversation(title, userId)
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI conversation."));

        ChatMessage userMessage = chatMessageRepository.insertMessage(content, "user", conversation.getId())
                .orElseThrow(() -> new InternalServerErrorException("Failed to save user message."));

        List<ChatMessage> history = chatMessageRepository.findByConversationId(conversation.getId()).stream()
                .filter(msg -> !msg.getId().equals(userMessage.getId()))
                .toList();

        String aiResponse = aiApiService.sendMessage(content, history);

        ChatMessage agentMessage = chatMessageRepository.insertMessage(aiResponse, "agent", conversation.getId())
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI response message."));

        return chatMessageMapper.toResponseDto(agentMessage);
    }
}

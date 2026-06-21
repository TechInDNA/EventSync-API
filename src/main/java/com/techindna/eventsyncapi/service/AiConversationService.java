package com.techindna.eventsyncapi.service;

import com.openai.errors.InternalServerException;
import com.techindna.eventsyncapi.dto.ai.AiMessageInputDto;
import com.techindna.eventsyncapi.dto.ai.AiMessageResponseDto;
import com.techindna.eventsyncapi.entity.AiConversation;
import com.techindna.eventsyncapi.entity.Message;
import com.techindna.eventsyncapi.exception.InternalServerErrorException;
import com.techindna.eventsyncapi.mapper.AiMessageMapper;
import com.techindna.eventsyncapi.repository.AiConversationRepository;
import com.techindna.eventsyncapi.repository.MessageRepository;
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
    private final MessageRepository messageRepository;
    private final AiConversationsValidator aiConversationsValidator;
    private final AiMessageMapper aiMessageMapper;
    private final AiApiService aiApiService;

    @Transactional
    public AiMessageResponseDto createConversation(UUID userId, AiMessageInputDto request) {
        String content = aiConversationsValidator.validateUserRequest(request.getContent());

        String title = aiApiService.generateTitle(content)
                .orElseThrow(() -> new InternalServerErrorException("An error occurred during Title generation."));

        AiConversation conversation = aiConversationRepository.insertConversation(title, userId)
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI conversation."));

        Message userMessage = messageRepository.insertMessage(content, "user", conversation.getId())
                .orElseThrow(() -> new InternalServerErrorException("Failed to save user message."));

        List<Message> history = messageRepository.findByConversationId(conversation.getId()).stream()
                .filter(msg -> !msg.getId().equals(userMessage.getId()))
                .toList();

        String aiResponse = aiApiService.sendMessage(content, history);

        Message agentMessage = messageRepository.insertMessage(aiResponse, "agent", conversation.getId())
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI response message."));

        return aiMessageMapper.toResponseDto(agentMessage);
    }
}

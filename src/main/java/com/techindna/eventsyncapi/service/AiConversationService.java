package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.ai.AiConversationDetailResponseDto;
import com.techindna.eventsyncapi.dto.ai.AiConversationListResponseDto;
import com.techindna.eventsyncapi.dto.ai.ChatMessageInputDto;
import com.techindna.eventsyncapi.dto.ai.ChatMessageResponseDto;
import com.techindna.eventsyncapi.entity.AiConversation;
import com.techindna.eventsyncapi.entity.ChatMessage;
import com.techindna.eventsyncapi.exception.ForbiddenException;
import com.techindna.eventsyncapi.exception.InternalServerErrorException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.AiConversationMapper;
import com.techindna.eventsyncapi.repository.AiConversationRepository;
import com.techindna.eventsyncapi.repository.ChatMessageRepository;
import com.techindna.eventsyncapi.validator.AiConversationsValidator;
import com.techindna.eventsyncapi.validator.DataValidator;
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
    private final AiConversationMapper aiConversationMapper;
    private final AiApiService aiApiService;
    private final DataValidator dataValidator;

    @Transactional
    public ChatMessageResponseDto createConversation(UUID userId, ChatMessageInputDto request) {
        String content = aiConversationsValidator.validateUserRequest(request.getContent());

        String title = aiApiService.generateTitle(content)
                .orElseThrow(() -> new InternalServerErrorException("An error occurred during Title generation."));

        AiConversation conversation = aiConversationRepository.insertConversation(title, userId)
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI conversation."));

        chatMessageRepository.insertMessage(content, "user", conversation.getId())
                .orElseThrow(() -> new InternalServerErrorException("Failed to save user message."));

        String aiResponse = aiApiService.sendMessage(content, List.of());

        ChatMessage agentMessage = chatMessageRepository.insertMessage(aiResponse, "agent", conversation.getId())
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI response message."));

        return aiConversationMapper.toResponseDto(agentMessage);
    }

    @Transactional
    public ChatMessageResponseDto continueConversation(UUID conversationId, UUID userId, ChatMessageInputDto request) {
        String content = aiConversationsValidator.validateUserRequest(request.getContent());

        AiConversation conversation = aiConversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found."));

        if (!conversation.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this conversation.");
        }

        List<ChatMessage> history = chatMessageRepository.findByConversationId(conversationId);

        chatMessageRepository.insertMessage(content, "user", conversationId)
                .orElseThrow(() -> new InternalServerErrorException("Failed to save user message."));

        String aiResponse = aiApiService.sendMessage(content, history);

        ChatMessage agentMessage = chatMessageRepository.insertMessage(aiResponse, "agent", conversationId)
                .orElseThrow(() -> new InternalServerErrorException("Failed to save AI response message."));

        return aiConversationMapper.toResponseDto(agentMessage);
    }

    public AiConversationDetailResponseDto getConversation(UUID conversationId, UUID userId) {
        AiConversation conversation = aiConversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found."));

        if (!conversation.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this conversation.");
        }

        List<ChatMessage> messages = chatMessageRepository.findByConversationId(conversationId);

        return aiConversationMapper.toDetailResponseDto(conversation, messages);
    }

    @Transactional(readOnly = true)
    public AiConversationListResponseDto getConversations(UUID userId, int page, int size, String search) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;

        int offset = (page - 1) * size;

        dataValidator.validateSearchString(search);

        long total = aiConversationRepository.countByUserIdWithSearch(userId, search);
        List<AiConversation> conversations = aiConversationRepository.findByUserIdWithSearch(userId, search, size, offset);

        return aiConversationMapper.toListResponseDto(conversations, total, page, size);
    }

    @Transactional
    public void deleteConversation(UUID conversationId, UUID userId) {
        AiConversation conversation = aiConversationRepository.findById(conversationId)
                .orElseThrow(() -> new NotFoundException("Conversation not found."));

        if (!conversation.getUserId().equals(userId)) {
            throw new ForbiddenException("You do not have access to this conversation.");
        }

        aiConversationRepository.deleteConversationByIdAndUserId(conversationId, userId);
    }
}

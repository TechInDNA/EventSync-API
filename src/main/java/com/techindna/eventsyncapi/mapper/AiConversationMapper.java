package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.ai.AiConversationDetailResponseDto;
import com.techindna.eventsyncapi.dto.ai.AiConversationResponseDto;
import com.techindna.eventsyncapi.dto.ai.ChatMessageResponseDto;
import com.techindna.eventsyncapi.entity.AiConversation;
import com.techindna.eventsyncapi.entity.ChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class AiConversationMapper {

    private final ChatMessageMapper chatMessageMapper;

    public AiConversationResponseDto toResponseDto(AiConversation conversation) {
        if (conversation == null) return null;
        return AiConversationResponseDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .userId(conversation.getUserId())
                .createdAt(conversation.getCreatedAt())
                .build();
    }

    public ChatMessageResponseDto toResponseDto(ChatMessage message) {
        return chatMessageMapper.toResponseDto(message);
    }

    public AiConversationDetailResponseDto toDetailResponseDto(AiConversation conversation, List<ChatMessage> messages) {
        if (conversation == null) return null;
        List<ChatMessageResponseDto> messageDtos = messages == null
                ? null
                : messages.stream().map(chatMessageMapper::toResponseDto).toList();
        return AiConversationDetailResponseDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .userId(conversation.getUserId())
                .createdAt(conversation.getCreatedAt())
                .messages(messageDtos)
                .build();
    }
}

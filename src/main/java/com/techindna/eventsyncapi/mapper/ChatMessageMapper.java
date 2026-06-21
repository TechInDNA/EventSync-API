package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.ai.ChatMessageResponseDto;
import com.techindna.eventsyncapi.entity.ChatMessage;
import org.springframework.stereotype.Component;

@Component
public class ChatMessageMapper {

    public ChatMessageResponseDto toResponseDto(ChatMessage message) {
        if (message == null) return null;
        return ChatMessageResponseDto.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderType(message.getSenderType().name())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

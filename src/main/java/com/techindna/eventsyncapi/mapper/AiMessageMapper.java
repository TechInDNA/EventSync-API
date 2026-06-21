package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.ai.AiMessageResponseDto;
import com.techindna.eventsyncapi.entity.Message;
import org.springframework.stereotype.Component;

@Component
public class AiMessageMapper {

    public AiMessageResponseDto toResponseDto(Message message) {
        if (message == null) return null;
        return AiMessageResponseDto.builder()
                .id(message.getId())
                .conversationId(message.getConversationId())
                .senderType(message.getSenderType().name())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}

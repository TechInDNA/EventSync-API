package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.ai.AiConversationResponseDto;
import com.techindna.eventsyncapi.entity.AiConversation;
import org.springframework.stereotype.Component;

@Component
public class AiConversationMapper {

    public AiConversationResponseDto toResponseDto(AiConversation conversation) {
        if (conversation == null) return null;
        return AiConversationResponseDto.builder()
                .id(conversation.getId())
                .title(conversation.getTitle())
                .userId(conversation.getUserId())
                .createdAt(conversation.getCreatedAt())
                .build();
    }
}

package com.techindna.eventsyncapi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageResponseDto {

    private UUID id;
    private UUID conversationId;
    private String senderType;
    private String content;
    private Instant createdAt;
}

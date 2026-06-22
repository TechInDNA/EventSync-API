package com.techindna.eventsyncapi.dto.ai;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AiConversationDetailResponseDto {

    private UUID id;
    private String title;
    private UUID userId;
    private Instant createdAt;
    private List<ChatMessageResponseDto> messages;
}

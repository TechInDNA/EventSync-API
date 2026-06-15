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
public class AiConversationResponseDto {

    private UUID id;
    private String title;
    private String userRequest;
    private String aiResponse;
    private UUID userId;
    private Instant createdAt;
}

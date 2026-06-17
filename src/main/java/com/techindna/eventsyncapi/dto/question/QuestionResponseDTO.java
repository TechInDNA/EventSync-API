package com.techindna.eventsyncapi.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techindna.eventsyncapi.dto.auth.ParticipantRefDto;
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
public class QuestionResponseDto {
    private UUID id;
    private String title;
    private String content;
    private Instant createdAt;
    private UUID sessionId;
    @JsonProperty("isAnonymous")
    private boolean anonymous;
    private ParticipantRefDto participant;
    private int upvotes;
}

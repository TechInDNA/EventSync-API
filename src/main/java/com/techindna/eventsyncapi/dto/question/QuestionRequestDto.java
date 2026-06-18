package com.techindna.eventsyncapi.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDto {
    private String title;
    private String content;
    private UUID participantId;
    @JsonProperty("isAnonymous")
    private boolean anonymous;
}

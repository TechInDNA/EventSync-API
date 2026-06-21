package com.techindna.eventsyncapi.dto.question;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QuestionRequestDto {
    private String title;
    private String content;
    @JsonProperty("isAnonymous")
    private Boolean anonymous;
}

package com.techindna.eventsyncapi.dto.question;

import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.entity.User;
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
public class QuestionResponseDTO {
    private UUID id;
    private String title;
    private String content;
    private Instant createdAt;
    private Session session;
    private boolean anonymous;
    private User user;
}

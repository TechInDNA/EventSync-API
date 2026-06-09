package com.techindna.eventsyncapi.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthParticipantResponseDto {
    private String token;
    private ParticipantRefDto participant;
}

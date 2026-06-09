package com.techindna.eventsyncapi.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ParticipantRefDto {
    private UUID id;
    private String firstName;
    private String lastName;
    private String email;
}

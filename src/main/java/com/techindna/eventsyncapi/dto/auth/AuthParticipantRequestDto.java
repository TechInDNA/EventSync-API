package com.techindna.eventsyncapi.dto.auth;

import lombok.Data;

@Data
public class AuthParticipantRequestDto {

    private String firstName;
    private String lastName;
    private String email;
}

package com.techindna.eventsyncapi.dto.auth;

import com.techindna.eventsyncapi.dto.UserResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthLoginResponseDto {
    private String token;
    private UserResponseDto user;
}

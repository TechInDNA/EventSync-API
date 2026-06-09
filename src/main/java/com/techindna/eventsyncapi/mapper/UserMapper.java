package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.UserResponseDto;
import com.techindna.eventsyncapi.dto.auth.AuthParticipantRequestDto;
import com.techindna.eventsyncapi.dto.auth.ParticipantRefDto;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDto toResponseDto(User user) {
        if (user == null) return null;

        return UserResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .role(user.getRole())
                .build();
    }

    public User toParticipantEntity(AuthParticipantRequestDto request) {
        return User.builder()
                .firstName(request.getFirstName().strip())
                .lastName(request.getLastName().strip())
                .email(request.getEmail().strip())
                .role(Role.PARTICIPANT)
                .build();
    }

    public ParticipantRefDto toParticipantRef(User user) {
        return ParticipantRefDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .build();
    }
}

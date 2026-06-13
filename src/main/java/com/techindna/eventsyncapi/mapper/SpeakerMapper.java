package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.entity.enums.Role;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SpeakerMapper {

    private final ExternalLinkMapper externalLinkMapper;

    public ExternalLink toEntity(ExternalLinkDto dto, User user) {
        return externalLinkMapper.toEntity(dto, user);
    }

    public User toEntity(SpeakerInputDto dto) {
        return User.builder()
                .firstName(dto.getFirstName().strip())
                .lastName(dto.getLastName().strip())
                .email(dto.getEmail().strip())
                .profilePicture(dto.getProfilePicture() != null ? dto.getProfilePicture().strip() : null)
                .bio(dto.getBio() != null ? dto.getBio().strip() : null)
                .role(Role.SPEAKER)
                .build();
    }

    public SpeakerResponseDto toResponseDto(User user, List<ExternalLink> externalLinks) {
        if (user == null) return null;

        List<ExternalLinkDto> linkDtos = Optional.ofNullable(externalLinks)
                .orElse(Collections.emptyList())
                .stream()
                .map(externalLinkMapper::toDto)
                .toList();

        return SpeakerResponseDto.builder()
                .id(user.getId())
                .firstName(user.getFirstName())
                .lastName(user.getLastName())
                .email(user.getEmail())
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .externalLinks(linkDtos.isEmpty() ? null : linkDtos)
                .build();
    }
}

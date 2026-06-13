package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
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
                .profilePicture(user.getProfilePicture())
                .bio(user.getBio())
                .externalLinks(linkDtos)
                .build();
    }
}

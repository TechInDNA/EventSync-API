package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import org.springframework.stereotype.Component;

import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Component
public class SpeakerMapper {

    public SpeakerResponseDto toResponseDto(User user, List<ExternalLink> externalLinks) {
        if (user == null) return null;

        List<ExternalLinkDto> linkDtos = Optional.ofNullable(externalLinks)
                .orElse(Collections.emptyList())
                .stream()
                .map(this::toExternalLinkDto)
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

    public ExternalLinkDto toExternalLinkDto(ExternalLink link) {
        if (link == null) return null;
        return ExternalLinkDto.builder()
                .name(link.getName())
                .url(link.getUrl())
                .build();
    }
}

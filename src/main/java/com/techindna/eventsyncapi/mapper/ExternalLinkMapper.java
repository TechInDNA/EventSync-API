package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import org.springframework.stereotype.Component;

@Component
public class ExternalLinkMapper {

    public ExternalLink toEntity(ExternalLinkDto dto, User user) {
        if (dto == null) return null;
        return ExternalLink.builder()
                .name(dto.getName().strip())
                .url(dto.getUrl().strip())
                .user(user)
                .build();
    }

    public ExternalLinkDto toDto(ExternalLink link) {
        if (link == null) return null;
        return ExternalLinkDto.builder()
                .name(link.getName())
                .url(link.getUrl())
                .build();
    }
}

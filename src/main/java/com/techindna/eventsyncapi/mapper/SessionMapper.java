package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SpeakerRefDto;
import com.techindna.eventsyncapi.entity.Session;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Component
public class SessionMapper {

    private final EventMapper eventMapper;
    private final RoomMapper roomMapper;
    private final SpeakerMapper speakerMapper;

    public SessionMapper(EventMapper eventMapper, RoomMapper roomMapper, SpeakerMapper speakerMapper) {
        this.eventMapper = eventMapper;
        this.roomMapper = roomMapper;
        this.speakerMapper = speakerMapper;
    }

    public SessionResponseDto toResponseDto(Session session) {
        if (session == null) return null;

        Instant now = Instant.now();
        boolean isLive = session.getStartDate() != null && session.getEndDate() != null
                && now.isAfter(session.getStartDate()) && now.isBefore(session.getEndDate());

        List<SpeakerRefDto> speakers = Optional.ofNullable(session.getSpeakers())
                .orElse(List.of())
                .stream()
                .map(speakerMapper::toRefDto)
                .toList();

        return SessionResponseDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .room(roomMapper.toRefDto(session.getRoom()))
                .capacity(session.getCapacity())
                .event(eventMapper.toRefDto(session.getEvent()))
                .speakers(speakers)
                .isLive(isLive)
                .build();
    }
}

package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.event.SessionForEventDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.dto.session.EventRefDto;
import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SpeakerRefDto;
import com.techindna.eventsyncapi.dto.speaker.SessionForSpeakerDto;
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
                .speakers(speakers.isEmpty() ? null : speakers)
                .live(isLive)
                .build();
    }

    public SessionDetailResponseDto toDetailResponseDto(Session session, List<QuestionResponseDto> questions) {
        if (session == null) return null;

        Instant now = Instant.now();
        boolean isLive = session.getStartDate() != null && session.getEndDate() != null
                && now.isAfter(session.getStartDate()) && now.isBefore(session.getEndDate());

        List<SpeakerRefDto> speakers = Optional.ofNullable(session.getSpeakers())
                .orElse(List.of())
                .stream()
                .map(speakerMapper::toRefDto)
                .toList();

        return SessionDetailResponseDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .room(roomMapper.toRefDto(session.getRoom()))
                .capacity(session.getCapacity())
                .event(eventMapper.toRefDto(session.getEvent()))
                .speakers(speakers.isEmpty() ? null : speakers)
                .questions(questions == null || questions.isEmpty() ? null : questions)
                .live(isLive)
                .build();
    }

    public SessionForEventDto toEventSessionDto(Session session) {
        if (session == null) return null;

        Instant now = Instant.now();
        boolean isLive = session.getStartDate() != null && session.getEndDate() != null
                && now.isAfter(session.getStartDate()) && now.isBefore(session.getEndDate());

        return SessionForEventDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .room(roomMapper.toResponseDto(session.getRoom()))
                .capacity(session.getCapacity())
                .live(isLive)
                .build();
    }

    public SessionForSpeakerDto toSpeakerSessionDto(Session session) {
        if (session == null) return null;

        Instant now = Instant.now();
        boolean isLive = session.getStartDate() != null && session.getEndDate() != null
                && now.isAfter(session.getStartDate()) && now.isBefore(session.getEndDate());

        return SessionForSpeakerDto.builder()
                .id(session.getId())
                .title(session.getTitle())
                .description(session.getDescription())
                .startDate(session.getStartDate())
                .endDate(session.getEndDate())
                .room(roomMapper.toResponseDto(session.getRoom()))
                .capacity(session.getCapacity())
                .event(eventMapper.toRefDto(session.getEvent()))
                .live(isLive)
                .build();
    }
}

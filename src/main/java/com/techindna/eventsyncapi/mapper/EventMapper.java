package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventListResponseDto;
import com.techindna.eventsyncapi.dto.event.EventResponseDto;
import com.techindna.eventsyncapi.dto.event.SessionForEventDto;
import com.techindna.eventsyncapi.dto.session.EventRefDto;
import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.entity.Event;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.List;

@Component
public class EventMapper {

    public EventResponseDto toResponseDto(Event event) {
        if (event == null) return null;
        Instant now = Instant.now();
        boolean isLive = event.getStartDate() != null && event.getEndDate() != null
                && now.isAfter(event.getStartDate()) && now.isBefore(event.getEndDate());

        return EventResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .createdAt(event.getCreatedAt())
                .live(isLive)
                .build();
    }

    public EventRefDto toRefDto(Event event) {
        if (event == null) return null;
        return EventRefDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .build();
    }

    public EventListResponseDto toListResponseDto(List<Event> events, long total, int page, int size) {
        List<EventResponseDto> data = events.stream()
                .map(this::toResponseDto)
                .toList();
        MetaDto meta = MetaDto.builder()
                .total(total)
                .page(page)
                .size(size)
                .build();
        return EventListResponseDto.builder()
                .data(data)
                .meta(meta)
                .build();
    }

    public EventDetailResponseDto toDetailResponseDto(Event event, List<SessionForEventDto> sessions) {
        if (event == null) return null;
        Instant now = Instant.now();
        boolean isLive = event.getStartDate() != null && event.getEndDate() != null
                && now.isAfter(event.getStartDate()) && now.isBefore(event.getEndDate());

        return EventDetailResponseDto.builder()
                .id(event.getId())
                .title(event.getTitle())
                .description(event.getDescription())
                .startDate(event.getStartDate())
                .endDate(event.getEndDate())
                .location(event.getLocation())
                .createdAt(event.getCreatedAt())
                .live(isLive)
                .sessions(sessions.isEmpty() ? null : sessions)
                .build();
    }
}

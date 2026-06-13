package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.dto.event.EventListResponseDto;
import com.techindna.eventsyncapi.dto.event.EventResponseDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.mapper.EventMapper;
import com.techindna.eventsyncapi.repository.EventRepository;
import com.techindna.eventsyncapi.validator.EventValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final AuthService authService;
    private final EventValidator eventValidator;

    @Transactional(readOnly = true)
    public EventListResponseDto getAllEvents(int page, int size, String title, String location,
                                             Instant startDate, Instant endDate,
                                             Boolean isLive, String ipAddress) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        int offset = (page - 1) * size;

        authService.checkBlacklist(ipAddress);
        eventValidator.validateGet(title, location);

        long total = eventRepository.countByFilters(title, location, startDate, endDate, isLive);
        List<Event> events = eventRepository.findByFilters(title, location, startDate, endDate, isLive, size, offset);

        return eventMapper.toListResponseDto(events, total, page, size);
    }

    @Transactional
    public EventResponseDto createEvent(EventInputDto request) {
        eventValidator.validatePost(request);

        return eventMapper.toResponseDto(
                eventRepository.insertEvent(
                        request.getTitle().strip(),
                        request.getDescription().strip(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getLocation().strip()
                ).orElseThrow(() -> new ConflictException(
                        String.format("Event '%s' already exists.", request.getTitle())
                ))
        );
    }
}

package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.EventListResponseDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.mapper.EventMapper;
import com.techindna.eventsyncapi.repository.EventRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final AuthService authService;
    private final DataValidator dataValidator;

    @Transactional(readOnly = true)
    public EventListResponseDto getAllEvents(int page, int size, String title, String location, String ipAddress) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        int offset = (page - 1) * size;

        authService.checkBlacklist(ipAddress);

        if (title != null) {
            dataValidator.validateName("title", title, false);
        }
        if (location != null) {
            dataValidator.validateName("location", location, false);
        }

        long total = eventRepository.countByFilters(title, location);
        List<Event> events = eventRepository.findByFilters(title, location, size, offset);

        return eventMapper.toListResponseDto(events, total, page, size);
    }
}

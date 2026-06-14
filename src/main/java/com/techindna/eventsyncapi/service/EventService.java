package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.dto.event.EventListResponseDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.EventMapper;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.EventRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.validator.EventValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class EventService {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final AuthService authService;
    private final EventValidator eventValidator;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private static final String UNIQUE_CONSTRAINT_VIOLATION = "23505";

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
    public EventDetailResponseDto createEvent(EventInputDto request) {
        eventValidator.validateUpdate(request);

        return eventMapper.toDetailResponseDto(
                eventRepository.insertEvent(
                        request.getTitle().strip(),
                        request.getDescription().strip(),
                        request.getStartDate(),
                        request.getEndDate(),
                        request.getLocation().strip()
                ).orElseThrow(() -> new ConflictException(
                        String.format("Event '%s' already exists.", request.getTitle())
                )),
                List.of()
        );
    }

    @Transactional
    public EventDetailResponseDto updateEvent(UUID id, EventInputDto request) {
        eventValidator.validateUpdate(request);

        var updated = updateEventOrThrow(id, request);

        var sessions = sessionRepository.findByEventId(id).stream()
                .map(sessionMapper::toEventSessionDto)
                .toList();

        return eventMapper.toDetailResponseDto(updated, sessions);
    }

    private Event updateEventOrThrow(UUID id, EventInputDto request) {
        try {
            return eventRepository.updateEventById(
                    id,
                    request.getTitle().strip(),
                    request.getDescription().strip(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getLocation().strip()
            ).orElseThrow(() -> new NotFoundException(
                    String.format("Event %s not found.", id)));
        } catch (DataIntegrityViolationException e) {
            if (uniqueViolation(e)) {
                throw new ConflictException(
                        "Event '" + request.getTitle().strip() + "' already exists.");
            }
            throw e;
        }
    }

    private static boolean uniqueViolation(DataIntegrityViolationException e) {
        return e.getRootCause() instanceof SQLException sqlEx
                && UNIQUE_CONSTRAINT_VIOLATION.equals(sqlEx.getSQLState());
    }

    @Transactional
    public void deleteEvent(UUID id) {
        eventRepository.deleteEventById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Event %s not found.", id)));
    }
}

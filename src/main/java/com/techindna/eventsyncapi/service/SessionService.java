package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.validator.SessionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionValidator sessionValidator;

    @Transactional
    public SessionResponseDto createSession(SessionInputDto request) {
        sessionValidator.validateSession(request);

        var existence = sessionRepository.findRoomAndEventExistence(
                request.getRoomId(), request.getEventId()
        );
        if (existence.getRoomId() == null || existence.getEventId() == null) {
            throw new NotFoundException(
                    String.format("Room (%s) or event (%s) not found.",
                            request.getRoomId(), request.getEventId())
            );
        }

        return sessionMapper.toResponseDto(sessionRepository.insertSession(
                request.getTitle().strip(),
                request.getDescription().strip(),
                request.getStartDate(),
                request.getEndDate(),
                request.getRoomId(),
                request.getCapacity(),
                request.getEventId()
        ).orElseThrow(() -> new ConflictException(
                String.format("Session '%s' already exists.", request.getTitle())
        )));
    }

    @Transactional
    public void deleteSession(UUID id) {
        sessionRepository.deleteSessionById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Session %s not found.", id)));
    }
}

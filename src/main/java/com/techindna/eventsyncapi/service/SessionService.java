package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.validator.SessionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionValidator sessionValidator;
    private static final String UNIQUE_CONSTRAINT_VIOLATION = "23505";

    @Transactional
    public SessionResponseDto createSession(SessionInputDto request) {
        sessionValidator.validateCreation(request);

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

    @Transactional
    public SessionResponseDto updateSession(UUID id, SessionUpdateInputDto request) {
        sessionValidator.validateUpdate(request);
        return sessionMapper.toResponseDto(updateSessionOrThrow(id, request));
    }

    private Session updateSessionOrThrow(UUID id, SessionUpdateInputDto request) {
        var existence = sessionRepository.findRoomAndEventExistence(
                request.getRoomId(), request.getEventId()
        );
        if (existence.getRoomId() == null || existence.getEventId() == null) {
            throw new NotFoundException(
                    String.format("Room (%s) or event (%s) not found.",
                            request.getRoomId(), request.getEventId())
            );
        }

        try {
            return sessionRepository.updateSessionById(
                    id,
                    request.getTitle().strip(),
                    request.getDescription().strip(),
                    request.getStartDate(),
                    request.getEndDate(),
                    request.getRoomId(),
                    request.getCapacity(),
                    request.getEventId()
            ).orElseThrow(() -> new NotFoundException(
                    String.format("Session %s not found.", id)));
        } catch (DataIntegrityViolationException e) {
            if (uniqueViolation(e)) {
                throw new ConflictException(
                        "Session '" + request.getTitle().strip() + "' already exists.");
            }
            throw e;
        }
    }

    private static boolean uniqueViolation(DataIntegrityViolationException e) {
        return e.getRootCause() instanceof SQLException sqlEx
                && UNIQUE_CONSTRAINT_VIOLATION.equals(sqlEx.getSQLState());
    }
}

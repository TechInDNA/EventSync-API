package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionListResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.entity.Session;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.QuestionMapper;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.validator.SessionValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SessionService {

    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final SessionValidator sessionValidator;
    private final AuthService authService;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private static final String UNIQUE_CONSTRAINT_VIOLATION = "23505";

    @Transactional(readOnly = true)
    public SessionListResponseDto getAllSessions(int page, int size, String room, String event,
                                                  String speaker, Boolean live, String ipAddress) {
        if (page < 1) page = 1;
        if (size < 1) size = 20;

        int offset = (page - 1) * size;

        authService.checkBlacklist(ipAddress);
        sessionValidator.validateGet(room, speaker, event);

        long total = sessionRepository.countByFilters(room, event, speaker, live);
        List<Session> sessions = sessionRepository.findByFilters(room, event, speaker, live, size, offset);

        if (!sessions.isEmpty()) {
            sessions = sessionRepository.findAllByIdInWithDetails(
                    sessions
                            .stream()
                            .map(Session::getId)
                            .toList()
            );
        }

        return sessionMapper.toListResponseDto(sessions, total, page, size);
    }

    @Transactional(readOnly = true)
    public SessionDetailResponseDto getSessionById(UUID id, String ipAddress) {
        authService.checkBlacklist(ipAddress);

        Session session = sessionRepository.findByIdWithDetails(id)
                .orElseThrow(() -> new NotFoundException(String.format("Session %s not found.", id)));

        List<QuestionResponseDto> questionDtos = questionRepository.findBySessionId(id)
                .stream()
                .map(q -> questionMapper.toResponseDto(q, q.getUpvoteCount()))
                .toList();

        return sessionMapper.toDetailResponseDto(session, questionDtos.isEmpty() ? null : questionDtos);
    }

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
    public String addSpeakerToSession(UUID sessionId, UUID speakerId, SessionSpeakerInputDto request) {
        sessionValidator.validateAddSpeaker(request);

        sessionRepository.insertSessionSpeaker(
                sessionId, speakerId, request.getStartTime(), request.getEndTime()
        ).orElseThrow(() -> new NotFoundException(
                String.format("Session (%s) or speaker (%s) not found.", sessionId, speakerId)));

        return "Speaker linked to session.";
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

package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.dto.question.QuestionRequestDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.entity.Question;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.QuestionMapper;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final SessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final AuthService authService;
    private final DataValidator dataValidator;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public QuestionListResponseDto getQuestionsBySessionId(UUID sessionId, int page, int size, String sort, String title, String ipAddress) {

        if (page < 1) page = 1;
        if (size < 1) size = 20;

        authService.checkBlacklist(ipAddress);

        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(String.format("Session %s not found.", sessionId)));

        dataValidator.validateSearchString(title);

        int offset = (page - 1) * size;
        long total = questionRepository.countBySessionId(sessionId, title);
        List<Question> questions = questionRepository.findBySessionIdWithPagination(sessionId, sort, title, size, offset);

        Map<UUID, Integer> upvoteCounts = questionRepository.countUpvotesBySessionId(sessionId, title)
                .stream()
                .collect(Collectors.toMap(
                        row -> (UUID) row[0],
                        row -> ((Number) row[1]).intValue()
                ));

        return questionMapper.toListResponseDto(questions, upvoteCounts, total, page, size);
    }

    @Transactional
    public QuestionResponseDto createQuestion(UUID sessionId, QuestionRequestDto request, String ipAddress) {
        authService.checkBlacklist(ipAddress);

        dataValidator.validateText("content", request.getContent());

        String title = request.getTitle();
        if (title == null || title.isBlank()) {
            title = request.getContent().length() > 50
                    ? request.getContent().substring(0, 50).stripTrailing()
                    : request.getContent();
        }
        dataValidator.validateString("title", title);

        sessionRepository.findById(sessionId)
                .orElseThrow(() -> new NotFoundException(String.format("Session %s not found.", sessionId)));

        UUID participantId = request.getParticipantId();
        UUID userId = participantId != null
                ? participantId
                : UUID.fromString(SecurityContextHolder.getContext().getAuthentication().getPrincipal().toString());

        userRepository.findById(userId)
                .orElseThrow(() -> new UnprocessableEntityException(
                        String.format("User %s not found.", userId)));

        Question question = questionRepository.insertQuestion(
                title.strip(),
                request.getContent().strip(),
                sessionId,
                userId,
                request.isAnonymous()
        );

        return questionMapper.toResponseDto(question, 0);
    }
}

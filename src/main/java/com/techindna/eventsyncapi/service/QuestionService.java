package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.entity.Question;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.QuestionMapper;
import com.techindna.eventsyncapi.repository.QuestionRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class QuestionService {

    private final SessionRepository sessionRepository;
    private final QuestionRepository questionRepository;
    private final QuestionMapper questionMapper;
    private final AuthService authService;
    private final DataValidator dataValidator;

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

        return questionMapper.toListResponseDto(questions, total, page, size);
    }
}

package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.question.QuestionRequestDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class QuestionValidator {
    private final DataValidator dataValidator;

    public void validateUpdate(QuestionRequestDto request) {
        dataValidator.validateString("title", request.getTitle());
        dataValidator.validateText("content", request.getContent());
    }
}

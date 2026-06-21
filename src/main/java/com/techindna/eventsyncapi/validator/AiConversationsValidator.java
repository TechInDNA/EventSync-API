package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConversationsValidator {

    private final DataValidator dataValidator;

    public String validateUserRequest(String content) {
        dataValidator.checkNullData("content", content);
        dataValidator.validateText("content", content.strip());
        return content;
    }
}

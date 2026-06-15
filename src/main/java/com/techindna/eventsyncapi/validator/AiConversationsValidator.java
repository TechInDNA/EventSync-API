package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConversationsValidator {

    private final DataValidator dataValidator;

    public String validateUserRequest(String userRequest) {
        dataValidator.checkNullData("userRequest", userRequest);
        dataValidator.validateText("userRequest", userRequest);

        String stripped = userRequest.strip();
        if (stripped.isBlank()) {
            throw new UnprocessableEntityException("The field userRequest cannot be blank.");
        }

        return stripped;
    }
}

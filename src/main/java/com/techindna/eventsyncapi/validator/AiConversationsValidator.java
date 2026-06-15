package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.exception.BadRequestException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AiConversationsValidator {

    private static final int TITLE_MAX_LENGTH = 100;

    private final DataValidator dataValidator;

    /**
     * Validates the userRequest using the general {@link DataValidator},
     * checks that the stripped value is not blank, and returns a truncated title
     * suitable for the ai_conversation record.
     *
     * @param userRequest the raw user request from the input DTO
     * @return a non-blank title string, truncated to {@code TITLE_MAX_LENGTH} if necessary
     */
    public String validateAndGetTitle(String userRequest) {
        dataValidator.checkNullData("userRequest", userRequest);
        dataValidator.validateText("userRequest", userRequest);

        String stripped = userRequest.strip();
        if (stripped.isBlank()) {
            throw new BadRequestException("The field userRequest cannot be blank.");
        }

        return stripped.length() > TITLE_MAX_LENGTH
                ? stripped.substring(0, TITLE_MAX_LENGTH - 3).stripTrailing() + "..."
                : stripped;
    }
}

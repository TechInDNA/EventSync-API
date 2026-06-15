package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateInputDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SpeakerValidator {
    private final DataValidator dataValidator;

    public void validateUpdate(SpeakerUpdateInputDto speaker) {
        dataValidator.validateName("firstName", speaker.getFirstName());
        dataValidator.validateName("lastName", speaker.getLastName());
        dataValidator.validateEmail(speaker.getEmail());
        dataValidator.validateText("bio", speaker.getBio());
        dataValidator.validateUrl("profilePicture", speaker.getProfilePicture());
    }
}

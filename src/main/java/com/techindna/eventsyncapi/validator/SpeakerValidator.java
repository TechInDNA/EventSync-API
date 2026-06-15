package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
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

        if (speaker.getBio() != null) {
            dataValidator.validateText("bio", speaker.getBio());
        }
        if (speaker.getProfilePicture() != null) {
            dataValidator.validateUrl("profilePicture", speaker.getProfilePicture());
        }
    }

    public void validateCreation(SpeakerInputDto speaker) {
        dataValidator.validateName("firstName", speaker.getFirstName());
        dataValidator.validateName("lastName", speaker.getLastName());
        dataValidator.validateEmail(speaker.getEmail());
        dataValidator.validateText("bio", speaker.getBio());
        dataValidator.validateUrl("profilePicture", speaker.getProfilePicture());
    }
}

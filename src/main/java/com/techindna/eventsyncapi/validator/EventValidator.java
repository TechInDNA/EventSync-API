package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EventValidator {
    private final DataValidator dataValidator;

    public void validatePost(EventInputDto request) {
        dataValidator.validateString("title", request.getTitle());
        dataValidator.validateText("description", request.getDescription());
        dataValidator.checkNullData("startDate", request.getStartDate());
        dataValidator.checkNullData("endDate", request.getEndDate());
        dataValidator.validateName("location", request.getLocation());

        if (request.getEndDate() != null && request.getStartDate() != null
                && !request.getEndDate().isAfter(request.getStartDate())) {
            throw new UnprocessableEntityException(
                    "The field endDate must be after startDate."
            );
        }
    }

    public void validateGet(String title, String location){
        dataValidator.validateSearchString(title);
        dataValidator.validateSearchString(location);
    }
}

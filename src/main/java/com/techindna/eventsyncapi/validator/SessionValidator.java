package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SessionValidator {
    private final DataValidator dataValidator;

    public void validateSession(SessionInputDto session) {
        dataValidator.validateName("title", session.getTitle());
        dataValidator.validateText("description", session.getDescription());
        dataValidator.checkNullData("startDate", session.getStartDate());
        dataValidator.checkNullData("endDate", session.getEndDate());
        dataValidator.checkNullData("capacity", session.getCapacity());
        dataValidator.checkNullData("roomId", session.getRoomId());
        dataValidator.checkNullData("eventId", session.getEventId());

        if (session.getCapacity() <= 0) {
            throw new UnprocessableEntityException("The field capacity must be greater than 0.");
        }

        if (session.getEndDate() != null && session.getStartDate() != null
                && !session.getEndDate().isAfter(session.getStartDate())) {
            throw new UnprocessableEntityException("The field endDate must be after startDate.");
        }
    }

}

package com.techindna.eventsyncapi.validator;

import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class SessionValidator {
    private final DataValidator dataValidator;

    public void validateCreation(SessionInputDto session) {
        validateSession(session.getTitle(), session.getDescription(), session.getStartDate(), session.getEndDate(), session.getCapacity(), session.getRoomId(), session.getEventId());
    }

    private void validateSession(String title, String description, Instant startDate, Instant endDate, Integer capacity, UUID roomId, UUID eventId) {
        dataValidator.validateName("title", title);
        dataValidator.validateText("description", description);
        dataValidator.checkNullData("startDate", startDate);
        dataValidator.checkNullData("endDate", endDate);
        dataValidator.checkNullData("capacity", capacity);
        dataValidator.checkNullData("roomId", roomId);
        dataValidator.checkNullData("eventId", eventId);

        if (capacity <= 0) {
            throw new UnprocessableEntityException("The field capacity must be greater than 0.");
        }

        if (endDate != null && startDate != null
                && !endDate.isAfter(startDate)) {
            throw new UnprocessableEntityException("The field endDate must be after startDate.");
        }
    }

    public void validateUpdate(SessionUpdateInputDto session) {
        validateSession(session.getTitle(), session.getDescription(), session.getStartDate(), session.getEndDate(), session.getCapacity(), session.getRoomId(), session.getEventId());
    }

    public void validateAddSpeaker(SessionSpeakerInputDto request) {
        dataValidator.checkNullData("startTime", request.getStartTime());
        dataValidator.checkNullData("endTime", request.getEndTime());

        OffsetDateTime startTime = parseTimestamp(request.getStartTime(), "startTime");
        OffsetDateTime endTime = parseTimestamp(request.getEndTime(), "endTime");

        if (!endTime.isAfter(startTime)) {
            throw new UnprocessableEntityException("The field endTime must be after startTime.");
        }
    }

    private static OffsetDateTime parseTimestamp(String value, String fieldName) {
        try {
            return OffsetDateTime.parse(value, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        } catch (DateTimeParseException e) {
            throw new UnprocessableEntityException(
                    String.format("Invalid format for %s: expected ISO timestamp with timezone (yyyy-MM-ddTHH:mm:ss±HH:mm).", fieldName)
            );
        }
    }

    public void validateGet(String room, String speaker, String event) {
        dataValidator.validateSearchString(room);
        dataValidator.validateSearchString(speaker);
        dataValidator.validateSearchString(event);
    }

}

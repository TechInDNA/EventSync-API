package com.techindna.eventsyncapi.service.events;

import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.EventMapper;
import com.techindna.eventsyncapi.repository.EventRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.EventService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.EventValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostEventServiceTest {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String EVENT_TITLE = "Annual Tech Conference";
    private static final String EVENT_DESCRIPTION = "A great tech conference";
    private static final Instant START_DATE = Instant.parse("2025-06-01T09:00:00Z");
    private static final Instant END_DATE = Instant.parse("2025-06-03T18:00:00Z");
    private static final String EVENT_LOCATION = "Antananarivo";
    private static final Instant NOW = Instant.now();

    PostEventServiceTest() {
        eventRepository = mock(EventRepository.class);
        eventMapper = new EventMapper();
        eventService = new EventService(eventRepository, eventMapper, mock(AuthService.class), new EventValidator(new DataValidator()));
    }

    @Test
    @DisplayName("with valid input inserts and returns mapped response")
    void withValidInput_returnsCreatedEvent() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        var saved = Event.builder()
                .id(EVENT_ID)
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .createdAt(NOW)
                .build();

        when(eventRepository.insertEvent(EVENT_TITLE, EVENT_DESCRIPTION, START_DATE, END_DATE, EVENT_LOCATION))
                .thenReturn(Optional.of(saved));

        var result = eventService.createEvent(request);

        assertNotNull(result);
        assertEquals(EVENT_ID, result.getId());
        assertEquals(EVENT_TITLE, result.getTitle());
        assertEquals(EVENT_DESCRIPTION, result.getDescription());
        assertEquals(START_DATE, result.getStartDate());
        assertEquals(END_DATE, result.getEndDate());
        assertEquals(EVENT_LOCATION, result.getLocation());
        assertEquals(NOW, result.getCreatedAt());
        assertNull(result.getSessions());

        verify(eventRepository).insertEvent(EVENT_TITLE, EVENT_DESCRIPTION, START_DATE, END_DATE, EVENT_LOCATION);
    }

    @Test
    @DisplayName("with duplicate title throws ConflictException")
    void withDuplicateTitle_throwsConflictException() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        when(eventRepository.insertEvent(EVENT_TITLE, EVENT_DESCRIPTION, START_DATE, END_DATE, EVENT_LOCATION))
                .thenReturn(Optional.empty());

        var exception = assertThrows(ConflictException.class, () -> eventService.createEvent(request));

        assertEquals("Event 'Annual Tech Conference' already exists.", exception.getMessage());

        verify(eventRepository).insertEvent(EVENT_TITLE, EVENT_DESCRIPTION, START_DATE, END_DATE, EVENT_LOCATION);
    }

    @Test
    @DisplayName("with null title throws UnprocessableEntityException")
    void withNullTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(null)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with empty title throws UnprocessableEntityException")
    void withEmptyTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("")
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with blank title throws UnprocessableEntityException")
    void withBlankTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("   ")
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with title exceeding 50 characters throws UnprocessableEntityException")
    void withTooLongTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("ThisEventTitleIsWayTooLongAndShouldBeRejectedByTheValidator")
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with null description throws UnprocessableEntityException")
    void withNullDescription_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(null)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with null startDate throws UnprocessableEntityException")
    void withNullStartDate_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(null)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with null endDate throws UnprocessableEntityException")
    void withNullEndDate_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(null)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with endDate before startDate throws UnprocessableEntityException")
    void withEndDateBeforeStartDate_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(Instant.parse("2025-06-03T18:00:00Z"))
                .endDate(Instant.parse("2025-06-01T09:00:00Z"))
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with endDate equal to startDate throws UnprocessableEntityException")
    void withEndDateEqualToStartDate_throwsUnprocessable() {
        var now = Instant.parse("2025-06-01T09:00:00Z");
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(now)
                .endDate(now)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with null location throws UnprocessableEntityException")
    void withNullLocation_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(null)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with empty location throws UnprocessableEntityException")
    void withEmptyLocation_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with illegal characters in title throws UnprocessableEntityException")
    void withIllegalCharsInTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("<script>xss</script>")
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(EVENT_LOCATION)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("with illegal characters in location throws UnprocessableEntityException")
    void withIllegalCharsInLocation_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(EVENT_TITLE)
                .description(EVENT_DESCRIPTION)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("<script>alert(1)</script>")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.createEvent(request));

        verifyNoInteractions(eventRepository);
    }
}

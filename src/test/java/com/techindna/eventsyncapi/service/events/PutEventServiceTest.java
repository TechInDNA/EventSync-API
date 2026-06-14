package com.techindna.eventsyncapi.service.events;

import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.EventMapper;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.repository.EventRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.EventService;
import com.techindna.eventsyncapi.validator.DataValidator;
import com.techindna.eventsyncapi.validator.EventValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.dao.DataIntegrityViolationException;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PutEventServiceTest {

    private final EventRepository eventRepository;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final UUID ROOM_ID = UUID.fromString("b2c3d4e5-f6a7-8901-bcde-f12345678901");
    private static final Instant START_DATE = Instant.parse("2025-06-01T09:00:00Z");
    private static final Instant END_DATE = Instant.parse("2025-06-03T18:00:00Z");

    PutEventServiceTest() {
        eventRepository = mock(EventRepository.class);
        sessionRepository = mock(SessionRepository.class);
        sessionMapper = mock(SessionMapper.class);
        eventService = new EventService(eventRepository, new EventMapper(), mock(AuthService.class), new EventValidator(new DataValidator()), sessionRepository, sessionMapper);
    }

    @Test
    @DisplayName("updateEvent with existing id and valid data returns updated event")
    void updateEvent_withValidData_returnsUpdatedEvent() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        var updated = Event.builder()
                .id(EVENT_ID)
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .createdAt(Instant.now())
                .build();

        when(eventRepository.updateEventById(EVENT_ID, "Updated Conference", "An updated description",
                START_DATE, END_DATE, "Paris")).thenReturn(Optional.of(updated));
        when(sessionRepository.findByEventId(EVENT_ID)).thenReturn(List.of());

        var result = eventService.updateEvent(EVENT_ID, request);

        assertNotNull(result);
        assertEquals(EVENT_ID, result.getId());
        assertEquals("Updated Conference", result.getTitle());
        assertEquals("An updated description", result.getDescription());
        assertEquals(START_DATE, result.getStartDate());
        assertEquals(END_DATE, result.getEndDate());
        assertEquals("Paris", result.getLocation());

        verify(eventRepository).updateEventById(EVENT_ID, "Updated Conference", "An updated description",
                START_DATE, END_DATE, "Paris");
    }

    @Test
    @DisplayName("updateEvent with unknown id throws NotFoundException")
    void updateEvent_withUnknownId_throwsNotFound() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        when(eventRepository.updateEventById(EVENT_ID, "Updated Conference", "An updated description",
                START_DATE, END_DATE, "Paris")).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> eventService.updateEvent(EVENT_ID, request));
        assertEquals("Event " + EVENT_ID + " not found.", exception.getMessage());

        verify(eventRepository).updateEventById(EVENT_ID, "Updated Conference", "An updated description",
                START_DATE, END_DATE, "Paris");
    }

    @Test
    @DisplayName("updateEvent with null title throws UnprocessableEntityException")
    void updateEvent_withNullTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title(null)
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with empty title throws UnprocessableEntityException")
    void updateEvent_withEmptyTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with blank title throws UnprocessableEntityException")
    void updateEvent_withBlankTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("   ")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with illegal characters throws UnprocessableEntityException")
    void updateEvent_withIllegalChars_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("<script>xss</script>")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with title exceeding 50 characters throws UnprocessableEntityException")
    void updateEvent_withTooLongTitle_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("ThisEventTitleIsWayTooLongAndShouldBeRejectedByTheValidator")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with null description throws UnprocessableEntityException")
    void updateEvent_withNullDescription_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description(null)
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with null startDate throws UnprocessableEntityException")
    void updateEvent_withNullStartDate_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(null)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with null endDate throws UnprocessableEntityException")
    void updateEvent_withNullEndDate_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(null)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with null location throws UnprocessableEntityException")
    void updateEvent_withNullLocation_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location(null)
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }

    @Test
    @DisplayName("updateEvent with duplicate title throws ConflictException")
    void updateEvent_withDuplicateTitle_throwsConflict() {
        var request = EventInputDto.builder()
                .title("Existing Event")
                .description("An updated description")
                .startDate(START_DATE)
                .endDate(END_DATE)
                .location("Paris")
                .build();

        var dataIntegrityEx = new DataIntegrityViolationException(
                "could not execute statement",
                new org.postgresql.util.PSQLException(
                        "ERROR: duplicate key value violates unique constraint \"event_title_key\"\n" +
                                "  Detail: Key (title)=(Existing Event) already exists.",
                        org.postgresql.util.PSQLState.UNIQUE_VIOLATION
                )
        );

        when(eventRepository.updateEventById(EVENT_ID, "Existing Event", "An updated description",
                START_DATE, END_DATE, "Paris")).thenThrow(dataIntegrityEx);

        var ex = assertThrows(ConflictException.class, () -> eventService.updateEvent(EVENT_ID, request));
        assertEquals("Event 'Existing Event' already exists.", ex.getMessage());
    }

    @Test
    @DisplayName("updateEvent with endDate before startDate throws UnprocessableEntityException")
    void updateEvent_withEndDateBeforeStartDate_throwsUnprocessable() {
        var request = EventInputDto.builder()
                .title("Updated Conference")
                .description("An updated description")
                .startDate(END_DATE)
                .endDate(START_DATE)
                .location("Paris")
                .build();

        assertThrows(UnprocessableEntityException.class, () -> eventService.updateEvent(EVENT_ID, request));
        verifyNoInteractions(eventRepository);
    }
}

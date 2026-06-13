package com.techindna.eventsyncapi.service.events;

import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.exception.NotFoundException;
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

class DeleteEventServiceTest {

    private final EventRepository eventRepository;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    DeleteEventServiceTest() {
        eventRepository = mock(EventRepository.class);
        eventService = new EventService(eventRepository, new EventMapper(), mock(AuthService.class), new EventValidator(new DataValidator()));
    }

    @Test
    @DisplayName("deleteEvent with existing id deletes and returns void")
    void deleteEvent_withExistingId_deletesSuccessfully() {
        var event = Event.builder()
                .id(EVENT_ID)
                .title("Annual Tech Conference")
                .description("A great tech conference")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                .location("Antananarivo")
                .createdAt(Instant.now())
                .build();
        when(eventRepository.deleteEventById(EVENT_ID)).thenReturn(Optional.of(event));

        assertDoesNotThrow(() -> eventService.deleteEvent(EVENT_ID));

        verify(eventRepository).deleteEventById(EVENT_ID);
    }

    @Test
    @DisplayName("deleteEvent with unknown id throws NotFoundException")
    void deleteEvent_withUnknownId_throwsNotFound() {
        when(eventRepository.deleteEventById(EVENT_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> eventService.deleteEvent(EVENT_ID));
        assertEquals("Event " + EVENT_ID + " not found.", exception.getMessage());

        verify(eventRepository).deleteEventById(EVENT_ID);
    }
}

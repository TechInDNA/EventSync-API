package com.techindna.eventsyncapi.service.events;

import com.techindna.eventsyncapi.dto.EventListResponseDto;
import com.techindna.eventsyncapi.entity.Event;
import com.techindna.eventsyncapi.mapper.EventMapper;
import com.techindna.eventsyncapi.repository.EventRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.EventService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetEventServiceTest {

    private final EventRepository eventRepository;
    private final EventMapper eventMapper;
    private final AuthService authService;
    private final DataValidator dataValidator;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String EVENT_TITLE = "Conference 2025";
    private static final String EVENT_LOCATION = "Antananarivo";
    private static final String TEST_IP = "127.0.0.1";

    GetEventServiceTest() {
        eventRepository = mock(EventRepository.class);
        eventMapper = new EventMapper();
        authService = mock(AuthService.class);
        dataValidator = mock(DataValidator.class);
        eventService = new EventService(eventRepository, eventMapper, authService, dataValidator);
    }

    @Test
    @DisplayName("getAllEvents returns mapped list with pagination")
    void getAllEvents_withValidPagination_returnsList() {
        var event = Event.builder()
                .id(EVENT_ID)
                .title(EVENT_TITLE)
                .description("A great event")
                .startDate(Instant.parse("2025-06-01T09:00:00Z"))
                .endDate(Instant.parse("2025-06-03T18:00:00Z"))
                .location(EVENT_LOCATION)
                .createdAt(Instant.parse("2025-01-01T00:00:00Z"))
                .build();
        when(eventRepository.countByFilters(null, null, null, null, null)).thenReturn(1L);
        when(eventRepository.findByFilters(null, null, null, null, null, 10, 0)).thenReturn(List.of(event));

        EventListResponseDto result = eventService.getAllEvents(1, 10, null, null, null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getSize());
        assertEquals(1, result.getData().size());
        assertEquals(EVENT_TITLE, result.getData().getFirst().getTitle());
        assertEquals(EVENT_LOCATION, result.getData().getFirst().getLocation());
        assertEquals(EVENT_ID, result.getData().getFirst().getId());
        assertFalse(result.getData().getFirst().isLive());

        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator, times(2)).validateSearchString(isNull());
        verify(eventRepository).countByFilters(null, null, null, null, null);
        verify(eventRepository).findByFilters(null, null, null, null, null, 10, 0);
    }

    @Test
    @DisplayName("getAllEvents with page 2 returns correct offset")
    void getAllEvents_withPage2_returnsCorrectOffset() {
        when(eventRepository.countByFilters(null, null, null, null, null)).thenReturn(5L);
        when(eventRepository.findByFilters(null, null, null, null, null, 10, 10)).thenReturn(List.of());

        EventListResponseDto result = eventService.getAllEvents(2, 10, null, null, null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(5, result.getMeta().getTotal());
        assertEquals(2, result.getMeta().getPage());
        assertTrue(result.getData().isEmpty());

        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator, times(2)).validateSearchString(isNull());
        verify(eventRepository).findByFilters(null, null, null, null, null, 10, 10);
    }

    @Test
    @DisplayName("getAllEvents with page < 1 defaults to 1")
    void getAllEvents_withInvalidPage_defaultsTo1() {
        when(eventRepository.countByFilters(null, null, null, null, null)).thenReturn(0L);
        when(eventRepository.findByFilters(null, null, null, null, null, 10, 0)).thenReturn(List.of());

        EventListResponseDto result = eventService.getAllEvents(0, 10, null, null, null, null, null, TEST_IP);

        assertEquals(1, result.getMeta().getPage());
        verify(authService).checkBlacklist(TEST_IP);
        verify(eventRepository).findByFilters(null, null, null, null, null, 10, 0);
    }

    @Test
    @DisplayName("getAllEvents with size < 1 defaults to 10")
    void getAllEvents_withInvalidSize_defaultsTo10() {
        when(eventRepository.countByFilters(null, null, null, null, null)).thenReturn(0L);
        when(eventRepository.findByFilters(null, null, null, null, null, 10, 0)).thenReturn(List.of());

        EventListResponseDto result = eventService.getAllEvents(1, 0, null, null, null, null, null, TEST_IP);

        assertEquals(10, result.getMeta().getSize());
        verify(authService).checkBlacklist(TEST_IP);
        verify(eventRepository).findByFilters(null, null, null, null, null, 10, 0);
    }

    @Test
    @DisplayName("getAllEvents with empty database returns empty list")
    void getAllEvents_withNoEvents_returnsEmptyList() {
        when(eventRepository.countByFilters(null, null, null, null, null)).thenReturn(0L);
        when(eventRepository.findByFilters(null, null, null, null, null, 10, 0)).thenReturn(List.of());

        EventListResponseDto result = eventService.getAllEvents(1, 10, null, null, null, null, null, TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
    }

    @Test
    @DisplayName("getAllEvents with title filter returns matching events")
    void getAllEvents_withTitleFilter_returnsMatchingEvents() {
        var event = Event.builder().id(EVENT_ID).title(EVENT_TITLE).build();
        when(eventRepository.countByFilters(EVENT_TITLE, null, null, null, null)).thenReturn(1L);
        when(eventRepository.findByFilters(EVENT_TITLE, null, null, null, null, 10, 0)).thenReturn(List.of(event));

        EventListResponseDto result = eventService.getAllEvents(1, 10, EVENT_TITLE, null, null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(EVENT_TITLE, result.getData().getFirst().getTitle());
        assertEquals(1, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator).validateSearchString(EVENT_TITLE);
        verify(eventRepository).countByFilters(EVENT_TITLE, null, null, null, null);
        verify(eventRepository).findByFilters(EVENT_TITLE, null, null, null, null, 10, 0);
        verifyNoMoreInteractions(eventRepository);
    }

    @Test
    @DisplayName("getAllEvents with title filter and no match returns empty list")
    void getAllEvents_withTitleFilter_noMatch_returnsEmpty() {
        when(eventRepository.countByFilters("Nonexistent", null, null, null, null)).thenReturn(0L);
        when(eventRepository.findByFilters("Nonexistent", null, null, null, null, 10, 0)).thenReturn(List.of());

        EventListResponseDto result = eventService.getAllEvents(1, 10, "Nonexistent", null, null, null, null, TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator).validateSearchString("Nonexistent");
        verify(eventRepository).countByFilters("Nonexistent", null, null, null, null);
        verify(eventRepository).findByFilters("Nonexistent", null, null, null, null, 10, 0);
    }

    @Test
    @DisplayName("getAllEvents with location filter returns matching events")
    void getAllEvents_withLocationFilter_returnsMatchingEvents() {
        var event = Event.builder().id(EVENT_ID).title(EVENT_TITLE).location(EVENT_LOCATION).build();
        when(eventRepository.countByFilters(null, EVENT_LOCATION, null, null, null)).thenReturn(1L);
        when(eventRepository.findByFilters(null, EVENT_LOCATION, null, null, null, 10, 0)).thenReturn(List.of(event));

        EventListResponseDto result = eventService.getAllEvents(1, 10, null, EVENT_LOCATION, null, null, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(EVENT_LOCATION, result.getData().getFirst().getLocation());
        assertEquals(1, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(dataValidator).validateSearchString(EVENT_LOCATION);
        verify(eventRepository).countByFilters(null, EVENT_LOCATION, null, null, null);
        verify(eventRepository).findByFilters(null, EVENT_LOCATION, null, null, null, 10, 0);
        verifyNoMoreInteractions(eventRepository);
    }
}

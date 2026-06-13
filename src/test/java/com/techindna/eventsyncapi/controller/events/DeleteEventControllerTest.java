package com.techindna.eventsyncapi.controller.events;

import com.techindna.eventsyncapi.controller.EventController;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.EventService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class DeleteEventControllerTest {

    private final MockMvc mockMvc;
    private final EventService eventService;

    private static final UUID EVENT_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    DeleteEventControllerTest() {
        eventService = mock(EventService.class);
        var controller = new EventController(eventService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("DELETE /events/{id} with valid id returns 204")
    void deleteEvent_withValidId_returns204() throws Exception {
        doNothing().when(eventService).deleteEvent(EVENT_ID);

        mockMvc.perform(delete("/events/{id}", EVENT_ID))
                .andExpect(status().isNoContent());

        verify(eventService).deleteEvent(EVENT_ID);
    }

    @Test
    @DisplayName("DELETE /events/{id} with unknown id returns 404")
    void deleteEvent_withUnknownId_returns404() throws Exception {
        doThrow(new NotFoundException("Event " + EVENT_ID + " not found."))
                .when(eventService).deleteEvent(EVENT_ID);

        mockMvc.perform(delete("/events/{id}", EVENT_ID))
                .andExpect(status().isNotFound());

        verify(eventService).deleteEvent(EVENT_ID);
    }
}

package com.techindna.eventsyncapi.controller.rooms;

import com.techindna.eventsyncapi.controller.RoomController;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class DeleteRoomControllerTest {

    private final MockMvc mockMvc;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    DeleteRoomControllerTest() {
        roomService = mock(RoomService.class);
        var controller = new RoomController(roomService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("DELETE /rooms/{id} with valid id returns 204")
    void deleteRoom_withValidId_returns204() throws Exception {
        doNothing().when(roomService).deleteRoom(ROOM_ID);

        mockMvc.perform(delete("/rooms/{id}", ROOM_ID))
                .andExpect(status().isNoContent());

        verify(roomService).deleteRoom(ROOM_ID);
    }

    @Test
    @DisplayName("DELETE /rooms/{id} with unknown id returns 404")
    void deleteRoom_withUnknownId_returns404() throws Exception {
        doThrow(new NotFoundException("Room " + ROOM_ID + " not found."))
                .when(roomService).deleteRoom(ROOM_ID);

        mockMvc.perform(delete("/rooms/{id}", ROOM_ID))
                .andExpect(status().isNotFound());

        verify(roomService).deleteRoom(ROOM_ID);
    }
}

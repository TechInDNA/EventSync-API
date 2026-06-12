package com.techindna.eventsyncapi.controller.rooms;

import com.techindna.eventsyncapi.controller.RoomController;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetRoomByIdControllerTest {

    private final MockMvc mockMvc;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    GetRoomByIdControllerTest() {
        roomService = mock(RoomService.class);
        var controller = new RoomController(roomService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /rooms/{id} with valid id returns 200")
    void getRoomById_withValidId_returns200() throws Exception {
        var response = RoomResponseDto.builder()
                .id(ROOM_ID)
                .name("Main Hall")
                .build();

        when(roomService.getRoomById(eq(ROOM_ID), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/rooms/{id}", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROOM_ID.toString()))
                .andExpect(jsonPath("$.name").value("Main Hall"));

        verify(roomService).getRoomById(eq(ROOM_ID), nullable(String.class));
    }

    @Test
    @DisplayName("GET /rooms/{id} with unknown id returns 404")
    void getRoomById_withUnknownId_returns404() throws Exception {
        when(roomService.getRoomById(eq(ROOM_ID), nullable(String.class)))
                .thenThrow(new NotFoundException("Room " + ROOM_ID + " not found."));

        mockMvc.perform(get("/rooms/{id}", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Room " + ROOM_ID + " not found."));

        verify(roomService).getRoomById(eq(ROOM_ID), nullable(String.class));
    }
}

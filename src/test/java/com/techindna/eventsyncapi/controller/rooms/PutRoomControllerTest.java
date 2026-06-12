package com.techindna.eventsyncapi.controller.rooms;

import com.techindna.eventsyncapi.controller.RoomController;
import com.techindna.eventsyncapi.dto.room.RoomInputDto;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import tools.jackson.databind.ObjectMapper;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;
import static org.mockito.Mockito.eq;

class PutRoomControllerTest {

    private final MockMvc mockMvc;
    private final ObjectMapper objectMapper;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PutRoomControllerTest() {
        roomService = mock(RoomService.class);
        objectMapper = new ObjectMapper();
        var controller = new RoomController(roomService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("PUT /rooms/{id} with valid body returns 200 and updated room")
    void updateRoom_withValidData_returns200() throws Exception {
        var request = RoomInputDto.builder().name("Renamed Hall").build();
        var response = RoomResponseDto.builder()
                .id(ROOM_ID)
                .name("Renamed Hall")
                .build();

        when(roomService.updateRoom(eq(ROOM_ID), any(RoomInputDto.class))).thenReturn(response);

        mockMvc.perform(put("/rooms/{id}", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(ROOM_ID.toString()))
                .andExpect(jsonPath("$.name").value("Renamed Hall"));
    }

    @Test
    @DisplayName("PUT /rooms/{id} with empty name returns 422")
    void updateRoom_withEmptyName_returns422() throws Exception {
        var request = RoomInputDto.builder().name("").build();

        when(roomService.updateRoom(eq(ROOM_ID), any(RoomInputDto.class)))
                .thenThrow(new UnprocessableEntityException("The field name is required and cannot be blank."));

        mockMvc.perform(put("/rooms/{id}", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isUnprocessableContent())
                .andExpect(jsonPath("$.status").value(422))
                .andExpect(jsonPath("$.error").value("Unprocessable Entity"));
    }

    @Test
    @DisplayName("PUT /rooms/{id} with unknown id returns 404")
    void updateRoom_withUnknownId_returns404() throws Exception {
        var request = RoomInputDto.builder().name("Renamed Hall").build();

        when(roomService.updateRoom(eq(ROOM_ID), any(RoomInputDto.class)))
                .thenThrow(new NotFoundException("Room " + ROOM_ID + " not found."));

        mockMvc.perform(put("/rooms/{id}", ROOM_ID)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(toJson(request)))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value(404))
                .andExpect(jsonPath("$.error").value("Not Found"))
                .andExpect(jsonPath("$.message").value("Room " + ROOM_ID + " not found."));
    }

    private String toJson(Object obj) {
        try {
            return objectMapper.writeValueAsString(obj);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}

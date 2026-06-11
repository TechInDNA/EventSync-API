package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.RoomResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class RoomControllerTest {

    private final MockMvc mockMvc;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    RoomControllerTest() {
        roomService = mock(RoomService.class);
        var controller = new RoomController(roomService);
        var exceptionHandler = new GlobalExceptionHandler();
        mockMvc = standaloneSetup(controller)
                .setControllerAdvice(exceptionHandler)
                .build();
    }

    @Test
    @DisplayName("GET /rooms returns 200 with paginated room list")
    void getAllRooms_withDefaultPagination_returns200AndList() throws Exception {
        var rooms = List.of(
                RoomResponseDto.builder().id(ROOM_ID).name("Main Hall").build()
        );
        var response = RoomListResponseDto.builder()
                .data(rooms)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(roomService.getAllRooms(1, 10)).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(ROOM_ID.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Main Hall"))
                .andExpect(jsonPath("$.meta.total").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(10));
    }

    @Test
    @DisplayName("GET /rooms with custom pagination returns 200")
    void getAllRooms_withCustomPagination_returns200() throws Exception {
        var rooms = List.of(
                RoomResponseDto.builder().id(ROOM_ID).name("Main Hall").build()
        );
        var response = RoomListResponseDto.builder()
                .data(rooms)
                .meta(MetaDto.builder().total(1).page(2).size(5).build())
                .build();

        when(roomService.getAllRooms(2, 5)).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .param("page", "2")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.size").value(5));
    }

    @Test
    @DisplayName("GET /rooms with empty list returns 200 and empty data")
    void getAllRooms_whenEmpty_returns200WithEmptyList() throws Exception {
        var response = RoomListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(10).build())
                .build();

        when(roomService.getAllRooms(1, 10)).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.meta.total").value(0));
    }
}

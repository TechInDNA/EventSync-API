package com.techindna.eventsyncapi.controller.rooms;

import com.techindna.eventsyncapi.controller.RoomController;
import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.room.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.exception.GlobalExceptionHandler;
import com.techindna.eventsyncapi.service.RoomService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.ArgumentMatchers.nullable;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.setup.MockMvcBuilders.standaloneSetup;

class GetRoomControllerTest {

    private final MockMvc mockMvc;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    GetRoomControllerTest() {
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

        when(roomService.getAllRooms(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].id").value(ROOM_ID.toString()))
                .andExpect(jsonPath("$.data[0].name").value("Main Hall"))
                .andExpect(jsonPath("$.meta.total").value(1))
                .andExpect(jsonPath("$.meta.page").value(1))
                .andExpect(jsonPath("$.meta.size").value(10));

        verify(roomService).getAllRooms(eq(1), eq(10), isNull(), nullable(String.class));
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

        when(roomService.getAllRooms(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .param("page", "2")
                        .param("size", "5")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.meta.page").value(2))
                .andExpect(jsonPath("$.meta.size").value(5));

        verify(roomService).getAllRooms(eq(2), eq(5), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /rooms with empty list returns 200 and empty data")
    void getAllRooms_whenEmpty_returns200WithEmptyList() throws Exception {
        var response = RoomListResponseDto.builder()
                .data(List.of())
                .meta(MetaDto.builder().total(0).page(1).size(10).build())
                .build();

        when(roomService.getAllRooms(anyInt(), anyInt(), any(), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.meta.total").value(0));

        verify(roomService).getAllRooms(eq(1), eq(10), isNull(), nullable(String.class));
    }

    @Test
    @DisplayName("GET /rooms with name filter returns 200 and filtered results")
    void getAllRooms_withNameFilter_returns200() throws Exception {
        var rooms = List.of(
                RoomResponseDto.builder().id(ROOM_ID).name("Main Hall").build()
        );
        var response = RoomListResponseDto.builder()
                .data(rooms)
                .meta(MetaDto.builder().total(1).page(1).size(10).build())
                .build();

        when(roomService.getAllRooms(anyInt(), anyInt(), eq("Main"), nullable(String.class))).thenReturn(response);

        mockMvc.perform(get("/rooms")
                        .param("name", "Main")
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data[0].name").value("Main Hall"))
                .andExpect(jsonPath("$.meta.total").value(1));

        verify(roomService).getAllRooms(eq(1), eq(10), eq("Main"), nullable(String.class));
    }
}

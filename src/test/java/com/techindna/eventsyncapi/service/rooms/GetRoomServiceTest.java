package com.techindna.eventsyncapi.service.rooms;

import com.techindna.eventsyncapi.dto.RoomListResponseDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.mapper.RoomMapper;
import com.techindna.eventsyncapi.repository.RoomRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.RoomService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetRoomServiceTest {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final AuthService authService;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String VALID_ROOM_NAME = "Main Hall";
    private static final String TEST_IP = "127.0.0.1";

    GetRoomServiceTest() {
        roomRepository = mock(RoomRepository.class);
        roomMapper = new RoomMapper();
        authService = mock(AuthService.class);
        roomService = new RoomService(roomRepository, roomMapper, new DataValidator(), authService);
    }

    @Test
    @DisplayName("getAllRooms returns mapped list with pagination")
    void getAllRooms_withValidPagination_returnsList() {
        var room = Room.builder().id(ROOM_ID).name(VALID_ROOM_NAME).build();
        when(roomRepository.countByNameContaining("")).thenReturn(1L);
        when(roomRepository.findByNameContaining("", 10, 0)).thenReturn(List.of(room));

        RoomListResponseDto result = roomService.getAllRooms(1, 10, null, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getSize());
        assertEquals(1, result.getData().size());
        assertEquals(VALID_ROOM_NAME, result.getData().getFirst().getName());
        assertEquals(ROOM_ID, result.getData().getFirst().getId());

        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).countByNameContaining("");
        verify(roomRepository).findByNameContaining("", 10, 0);
    }

    @Test
    @DisplayName("getAllRooms with page 2 returns correct offset")
    void getAllRooms_withPage2_returnsCorrectOffset() {
        when(roomRepository.countByNameContaining("")).thenReturn(5L);
        when(roomRepository.findByNameContaining("", 10, 10)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(2, 10, null, TEST_IP);

        assertNotNull(result);
        assertEquals(5, result.getMeta().getTotal());
        assertEquals(2, result.getMeta().getPage());
        assertTrue(result.getData().isEmpty());

        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).findByNameContaining("", 10, 10);
    }

    @Test
    @DisplayName("getAllRooms with page < 1 defaults to 1")
    void getAllRooms_withInvalidPage_defaultsTo1() {
        when(roomRepository.countByNameContaining("")).thenReturn(0L);
        when(roomRepository.findByNameContaining("", 10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(0, 10, null, TEST_IP);

        assertEquals(1, result.getMeta().getPage());
        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).findByNameContaining("", 10, 0);
    }

    @Test
    @DisplayName("getAllRooms with size < 1 defaults to 10")
    void getAllRooms_withInvalidSize_defaultsTo10() {
        when(roomRepository.countByNameContaining("")).thenReturn(0L);
        when(roomRepository.findByNameContaining("", 10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(1, 0, null, TEST_IP);

        assertEquals(10, result.getMeta().getSize());
        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).findByNameContaining("", 10, 0);
    }

    @Test
    @DisplayName("getAllRooms with empty database returns empty list")
    void getAllRooms_withNoRooms_returnsEmptyList() {
        when(roomRepository.countByNameContaining("")).thenReturn(0L);
        when(roomRepository.findByNameContaining("", 10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(1, 10, null, TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
    }

    @Test
    @DisplayName("getAllRooms with name filter returns matching rooms")
    void getAllRooms_withNameFilter_returnsMatchingRooms() {
        var room = Room.builder().id(ROOM_ID).name(VALID_ROOM_NAME).build();
        when(roomRepository.countByNameContaining(VALID_ROOM_NAME)).thenReturn(1L);
        when(roomRepository.findByNameContaining(VALID_ROOM_NAME, 10, 0)).thenReturn(List.of(room));

        RoomListResponseDto result = roomService.getAllRooms(1, 10, VALID_ROOM_NAME, TEST_IP);

        assertNotNull(result);
        assertEquals(1, result.getData().size());
        assertEquals(VALID_ROOM_NAME, result.getData().getFirst().getName());
        assertEquals(1, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).countByNameContaining(VALID_ROOM_NAME);
        verify(roomRepository).findByNameContaining(VALID_ROOM_NAME, 10, 0);
        verifyNoMoreInteractions(roomRepository);
    }

    @Test
    @DisplayName("getAllRooms with name filter and no match returns empty list")
    void getAllRooms_withNameFilter_noMatch_returnsEmpty() {
        when(roomRepository.countByNameContaining("Nonexistent")).thenReturn(0L);
        when(roomRepository.findByNameContaining("Nonexistent", 10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(1, 10, "Nonexistent", TEST_IP);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());

        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).countByNameContaining("Nonexistent");
        verify(roomRepository).findByNameContaining("Nonexistent", 10, 0);
    }
}

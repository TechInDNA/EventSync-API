package com.techindna.eventsyncapi.service.rooms;

import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.RoomMapper;
import com.techindna.eventsyncapi.repository.RoomRepository;
import com.techindna.eventsyncapi.service.AuthService;
import com.techindna.eventsyncapi.service.RoomService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class GetRoomByIdServiceTest {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final AuthService authService;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");
    private static final String VALID_ROOM_NAME = "Main Hall";
    private static final String TEST_IP = "127.0.0.1";

    GetRoomByIdServiceTest() {
        roomRepository = mock(RoomRepository.class);
        roomMapper = new RoomMapper();
        authService = mock(AuthService.class);
        roomService = new RoomService(roomRepository, roomMapper, new DataValidator(), authService);
    }

    @Test
    @DisplayName("getRoomById with existing id returns mapped response")
    void getRoomById_withExistingId_returnsRoom() {
        var room = Room.builder().id(ROOM_ID).name(VALID_ROOM_NAME).build();
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.of(room));

        RoomResponseDto result = roomService.getRoomById(ROOM_ID, TEST_IP);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.getId());
        assertEquals(VALID_ROOM_NAME, result.getName());

        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).findById(ROOM_ID);
    }

    @Test
    @DisplayName("getRoomById with unknown id throws NotFoundException")
    void getRoomById_withUnknownId_throwsNotFound() {
        when(roomRepository.findById(ROOM_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> roomService.getRoomById(ROOM_ID, TEST_IP));
        assertEquals("Room " + ROOM_ID + " not found.", exception.getMessage());

        verify(authService).checkBlacklist(TEST_IP);
        verify(roomRepository).findById(ROOM_ID);
    }
}

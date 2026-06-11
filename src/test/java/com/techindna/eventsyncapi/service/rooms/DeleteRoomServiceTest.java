package com.techindna.eventsyncapi.service.rooms;

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

class DeleteRoomServiceTest {

    private final RoomRepository roomRepository;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    DeleteRoomServiceTest() {
        roomRepository = mock(RoomRepository.class);
        roomService = new RoomService(roomRepository, new RoomMapper(), new DataValidator(), mock(AuthService.class));
    }

    @Test
    @DisplayName("deleteRoom with existing id deletes and returns void")
    void deleteRoom_withExistingId_deletesSuccessfully() {
        var room = Room.builder().id(ROOM_ID).name("Main Hall").build();
        when(roomRepository.deleteRoomById(ROOM_ID)).thenReturn(Optional.of(room));

        assertDoesNotThrow(() -> roomService.deleteRoom(ROOM_ID));

        verify(roomRepository).deleteRoomById(ROOM_ID);
    }

    @Test
    @DisplayName("deleteRoom with unknown id throws NotFoundException")
    void deleteRoom_withUnknownId_throwsNotFound() {
        when(roomRepository.deleteRoomById(ROOM_ID)).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> roomService.deleteRoom(ROOM_ID));
        assertEquals("Room " + ROOM_ID + " not found.", exception.getMessage());

        verify(roomRepository).deleteRoomById(ROOM_ID);
    }
}

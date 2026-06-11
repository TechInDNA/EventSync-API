package com.techindna.eventsyncapi.service.rooms;

import com.techindna.eventsyncapi.dto.RoomInputDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
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

class PutRoomServiceTest {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PutRoomServiceTest() {
        roomRepository = mock(RoomRepository.class);
        roomMapper = new RoomMapper();
        roomService = new RoomService(roomRepository, roomMapper, new DataValidator(), mock(AuthService.class));
    }

    @Test
    @DisplayName("updateRoom with existing id and valid name returns updated room")
    void updateRoom_withValidData_returnsUpdatedRoom() {
        var request = RoomInputDto.builder().name("Renamed Hall").build();
        var updated = Room.builder().id(ROOM_ID).name("Renamed Hall").build();

        when(roomRepository.updateRoomById(ROOM_ID, "Renamed Hall")).thenReturn(Optional.of(updated));

        var result = roomService.updateRoom(ROOM_ID, request);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.getId());
        assertEquals("Renamed Hall", result.getName());

        verify(roomRepository).updateRoomById(ROOM_ID, "Renamed Hall");
    }

    @Test
    @DisplayName("updateRoom with unknown id throws NotFoundException")
    void updateRoom_withUnknownId_throwsNotFound() {
        var request = RoomInputDto.builder().name("Renamed Hall").build();

        when(roomRepository.updateRoomById(ROOM_ID, "Renamed Hall")).thenReturn(Optional.empty());

        var exception = assertThrows(NotFoundException.class, () -> roomService.updateRoom(ROOM_ID, request));
        assertEquals("Room " + ROOM_ID + " not found.", exception.getMessage());

        verify(roomRepository).updateRoomById(ROOM_ID, "Renamed Hall");
    }

    @Test
    @DisplayName("updateRoom with null name throws UnprocessableEntityException")
    void updateRoom_withNullName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name(null).build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.updateRoom(ROOM_ID, request));
        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("updateRoom with empty name throws UnprocessableEntityException")
    void updateRoom_withEmptyName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.updateRoom(ROOM_ID, request));
        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("updateRoom with blank name throws UnprocessableEntityException")
    void updateRoom_withBlankName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("   ").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.updateRoom(ROOM_ID, request));
        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("updateRoom with illegal characters throws UnprocessableEntityException")
    void updateRoom_withIllegalChars_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("<script>xss</script>").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.updateRoom(ROOM_ID, request));
        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("updateRoom with name exceeding 50 characters throws UnprocessableEntityException")
    void updateRoom_withTooLongName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("ThisRoomNameIsWayTooLongAndShouldBeRejectedByTheValidator").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.updateRoom(ROOM_ID, request));
        verifyNoInteractions(roomRepository);
    }
}

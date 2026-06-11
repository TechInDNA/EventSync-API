package com.techindna.eventsyncapi.service.rooms;

import com.techindna.eventsyncapi.dto.RoomInputDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.RoomMapper;
import com.techindna.eventsyncapi.repository.RoomRepository;
import com.techindna.eventsyncapi.service.RoomService;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PostRoomServiceTest {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    PostRoomServiceTest() {
        roomRepository = mock(RoomRepository.class);
        roomMapper = new RoomMapper();
        roomService = new RoomService(roomRepository, roomMapper, new DataValidator());
    }

    @Test
    @DisplayName("with valid name inserts and returns mapped response")
    void withValidName_returnsCreatedRoom() {
        var request = RoomInputDto.builder().name("Workshop").build();
        var saved = Room.builder().id(ROOM_ID).name("Workshop").build();

        when(roomRepository.insertRoom("Workshop")).thenReturn(Optional.of(saved));

        var result = roomService.createRoom(request);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.getId());
        assertEquals("Workshop", result.getName());

        verify(roomRepository).insertRoom("Workshop");
    }

    @Test
    @DisplayName("with duplicate name throws ConflictException")
    void withDuplicateName_throwsConflictException() {
        var request = RoomInputDto.builder().name("Workshop").build();

        when(roomRepository.insertRoom("Workshop")).thenReturn(Optional.empty());

        var exception = assertThrows(ConflictException.class, () -> roomService.createRoom(request));

        assertEquals("Room Workshop already exists.", exception.getMessage());

        verify(roomRepository).insertRoom("Workshop");
    }

    @Test
    @DisplayName("with null name throws UnprocessableEntityException")
    void withNullName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name(null).build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.createRoom(request));

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("with empty name throws UnprocessableEntityException")
    void withEmptyName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.createRoom(request));

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("with blank name throws UnprocessableEntityException")
    void withBlankName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("   ").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.createRoom(request));

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("with illegal characters throws UnprocessableEntityException")
    void withIllegalChars_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("<script>xss</script>").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.createRoom(request));

        verifyNoInteractions(roomRepository);
    }

    @Test
    @DisplayName("with name exceeding 50 characters throws UnprocessableEntityException")
    void withTooLongName_throwsUnprocessable() {
        var request = RoomInputDto.builder().name("ThisRoomNameIsWayTooLongAndShouldBeRejectedByTheValidator").build();

        assertThrows(UnprocessableEntityException.class, () -> roomService.createRoom(request));

        verifyNoInteractions(roomRepository);
    }
}

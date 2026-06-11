package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.RoomInputDto;
import com.techindna.eventsyncapi.dto.RoomListResponseDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.mapper.RoomMapper;
import com.techindna.eventsyncapi.repository.RoomRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class RoomServiceTest {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final DataValidator dataValidator;
    private final RoomService roomService;

    private static final UUID ROOM_ID = UUID.fromString("a1b2c3d4-e5f6-7890-abcd-ef1234567890");

    RoomServiceTest() {
        roomRepository = mock(RoomRepository.class);
        roomMapper = new RoomMapper();
        dataValidator = mock(DataValidator.class);
        roomService = new RoomService(roomRepository, roomMapper, dataValidator);
    }

    @Test
    @DisplayName("getAllRooms returns mapped list with pagination")
    void getAllRooms_withValidPagination_returnsList() {
        var room = Room.builder().id(ROOM_ID).name("Main Hall").build();
        when(roomRepository.countAll()).thenReturn(1L);
        when(roomRepository.findAllPaginated(10, 0)).thenReturn(List.of(room));

        RoomListResponseDto result = roomService.getAllRooms(1, 10);

        assertNotNull(result);
        assertEquals(1, result.getMeta().getTotal());
        assertEquals(1, result.getMeta().getPage());
        assertEquals(10, result.getMeta().getSize());
        assertEquals(1, result.getData().size());
        assertEquals("Main Hall", result.getData().getFirst().getName());
        assertEquals(ROOM_ID, result.getData().getFirst().getId());

        verify(roomRepository).countAll();
        verify(roomRepository).findAllPaginated(10, 0);
    }

    @Test
    @DisplayName("getAllRooms with page 2 returns correct offset")
    void getAllRooms_withPage2_returnsCorrectOffset() {
        when(roomRepository.countAll()).thenReturn(5L);
        when(roomRepository.findAllPaginated(10, 10)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(2, 10);

        assertNotNull(result);
        assertEquals(5, result.getMeta().getTotal());
        assertEquals(2, result.getMeta().getPage());
        assertTrue(result.getData().isEmpty());

        verify(roomRepository).findAllPaginated(10, 10);
    }

    @Test
    @DisplayName("getAllRooms with page < 1 defaults to 1")
    void getAllRooms_withInvalidPage_defaultsTo1() {
        when(roomRepository.countAll()).thenReturn(0L);
        when(roomRepository.findAllPaginated(10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(0, 10);

        assertEquals(1, result.getMeta().getPage());
        verify(roomRepository).findAllPaginated(10, 0);
    }

    @Test
    @DisplayName("getAllRooms with size < 1 defaults to 10")
    void getAllRooms_withInvalidSize_defaultsTo10() {
        when(roomRepository.countAll()).thenReturn(0L);
        when(roomRepository.findAllPaginated(10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(1, 0);

        assertEquals(10, result.getMeta().getSize());
        verify(roomRepository).findAllPaginated(10, 0);
    }

    @Test
    @DisplayName("getAllRooms with empty database returns empty list")
    void getAllRooms_withNoRooms_returnsEmptyList() {
        when(roomRepository.countAll()).thenReturn(0L);
        when(roomRepository.findAllPaginated(10, 0)).thenReturn(List.of());

        RoomListResponseDto result = roomService.getAllRooms(1, 10);

        assertTrue(result.getData().isEmpty());
        assertEquals(0, result.getMeta().getTotal());
    }

    @Test
    @DisplayName("createRoom with valid name calls validator, inserts room, and returns mapped response")
    void createRoom_withValidName_returnsCreatedRoom() {
        var request = RoomInputDto.builder().name("Workshop A").build();
        var saved = Room.builder().id(ROOM_ID).name("Workshop A").build();

        when(roomRepository.insertRoom("Workshop A")).thenReturn(Optional.of(saved));

        var result = roomService.createRoom(request);

        assertNotNull(result);
        assertEquals(ROOM_ID, result.getId());
        assertEquals("Workshop A", result.getName());

        verify(dataValidator).validateName("name", "Workshop A", true);
        verify(roomRepository).insertRoom("Workshop A");
    }

    @Test
    @DisplayName("createRoom with duplicate name throws ConflictException")
    void createRoom_withDuplicateName_throwsConflictException() {
        var request = RoomInputDto.builder().name("Workshop A").build();

        when(roomRepository.insertRoom("Workshop A")).thenReturn(Optional.empty());

        var exception = assertThrows(ConflictException.class, () -> roomService.createRoom(request));

        assertEquals("Room Workshop A already exists.", exception.getMessage());

        verify(dataValidator).validateName("name", "Workshop A", true);
        verify(roomRepository).insertRoom("Workshop A");
    }
}

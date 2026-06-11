package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.RoomInputDto;
import com.techindna.eventsyncapi.dto.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.RoomResponseDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.RoomMapper;
import com.techindna.eventsyncapi.repository.BlacklistedIpRepository;
import com.techindna.eventsyncapi.repository.RoomRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final DataValidator dataValidator;
    private final AuthService authService;

    @Transactional(readOnly = true)
    public RoomListResponseDto getAllRooms(int page, int size, String searchByName, String ipAddress) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        int offset = (page - 1) * size;

        authService.checkBlacklist(ipAddress);

        String searchTerm = searchByName != null ? searchByName : "";

        long total = roomRepository.countByNameContaining(searchTerm);
        List<Room> rooms = roomRepository.findByNameContaining(searchTerm, size, offset);

        return roomMapper.toListResponseDto(rooms, total, page, size);
    }

    @Transactional(readOnly = true)
    public RoomResponseDto getRoomById(UUID id, String ipAddress) {
        authService.checkBlacklist(ipAddress);
        return roomMapper.toResponseDto(roomRepository.findById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Room %s not found.", id))));
    }

    @Transactional
    public RoomResponseDto createRoom(RoomInputDto request) {
        dataValidator.validateName("name", request.getName(), true);

        return roomMapper.toResponseDto(
                roomRepository.insertRoom(request.getName())
                        .orElseThrow(() -> new ConflictException(
                                String.format("Room %s already exists.", request.getName()))
                        )
        );
    }
}

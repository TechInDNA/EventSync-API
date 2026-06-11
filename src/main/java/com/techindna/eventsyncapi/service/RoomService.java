package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.RoomInputDto;
import com.techindna.eventsyncapi.dto.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.RoomResponseDto;
import com.techindna.eventsyncapi.entity.Room;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.mapper.RoomMapper;
import com.techindna.eventsyncapi.repository.RoomRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class RoomService {

    private final RoomRepository roomRepository;
    private final RoomMapper roomMapper;
    private final DataValidator dataValidator;

    @Transactional(readOnly = true)
    public RoomListResponseDto getAllRooms(int page, int size, String name) {
        if (page < 1) page = 1;
        if (size < 1) size = 10;

        int offset = (page - 1) * size;

        if (name != null && !name.isBlank()) {
            dataValidator.validateName("name", name, true);
        }

        String searchName = (name != null && !name.isBlank()) ? name : "";
        long total = roomRepository.countByNameContaining(searchName);
        List<Room> rooms = roomRepository.findByNameContaining(searchName, size, offset);

        return roomMapper.toListResponseDto(rooms, total, page, size);
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

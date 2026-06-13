package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.room.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import com.techindna.eventsyncapi.dto.session.RoomRefDto;
import com.techindna.eventsyncapi.entity.Room;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class RoomMapper {

    public RoomResponseDto toResponseDto(Room room) {
        if (room == null) return null;
        return RoomResponseDto.builder()
                .id(room.getId())
                .name(room.getName())
                .build();
    }

    public RoomRefDto toRefDto(Room room) {
        if (room == null) return null;
        return RoomRefDto.builder()
                .id(room.getId())
                .name(room.getName())
                .build();
    }

    public RoomListResponseDto toListResponseDto(List<Room> rooms, long total, int page, int size) {
        List<RoomResponseDto> data = rooms.stream()
                .map(this::toResponseDto)
                .toList();
        MetaDto meta = MetaDto.builder()
                .total(total)
                .page(page)
                .size(size)
                .build();
        return RoomListResponseDto.builder()
                .data(data)
                .meta(meta)
                .build();
    }
}

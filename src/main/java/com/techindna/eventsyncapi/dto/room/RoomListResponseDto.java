package com.techindna.eventsyncapi.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RoomListResponseDto {
    private List<RoomResponseDto> data;
    private MetaDto meta;
}

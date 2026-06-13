package com.techindna.eventsyncapi.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.techindna.eventsyncapi.dto.room.RoomResponseDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionForEventDto {
    private UUID id;
    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private RoomResponseDto room;
    private int capacity;

    @JsonProperty("isLive")
    private boolean live;
}

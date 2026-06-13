package com.techindna.eventsyncapi.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionResponseDto {

    private UUID id;
    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private RoomRefDto room;
    private int capacity;
    private EventRefDto event;
    private List<SpeakerRefDto> speakers;
    private boolean isLive;
}

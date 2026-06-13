package com.techindna.eventsyncapi.dto.event;

import com.fasterxml.jackson.annotation.JsonProperty;
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
public class EventDetailResponseDto {
    private UUID id;
    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private String location;
    private Instant createdAt;
    private List<SessionForEventDto> sessions;

    @JsonProperty("isLive")
    private boolean live;
}

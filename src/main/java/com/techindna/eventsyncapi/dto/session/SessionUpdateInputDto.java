package com.techindna.eventsyncapi.dto.session;

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
public class SessionUpdateInputDto {

    private String title;
    private String description;
    private Instant startDate;
    private Instant endDate;
    private Integer capacity;
    private UUID roomId;
    private UUID eventId;
}

package com.techindna.eventsyncapi.dto.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventInputDto {

    private String title;

    private String description;

    private Instant startDate;

    private Instant endDate;

    private String location;
}

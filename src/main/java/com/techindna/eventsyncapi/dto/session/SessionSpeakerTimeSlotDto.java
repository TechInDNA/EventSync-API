package com.techindna.eventsyncapi.dto.session;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SessionSpeakerTimeSlotDto {

    private String id;
    private String startTime;
    private String endTime;
}

package com.techindna.eventsyncapi.dto.event;

import com.techindna.eventsyncapi.dto.MetaDto;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventListResponseDto {
    private List<EventResponseDto> data;
    private MetaDto meta;
}

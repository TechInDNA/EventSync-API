package com.techindna.eventsyncapi.dto.speaker;

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
public class SpeakerListResponseDto {
    private List<SpeakerResponseDto> data;
    private MetaDto meta;
}

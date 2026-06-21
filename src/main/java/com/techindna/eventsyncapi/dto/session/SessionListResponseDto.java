package com.techindna.eventsyncapi.dto.session;

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
public class SessionListResponseDto {
    private List<SessionResponseDto> data;
    private MetaDto meta;
}
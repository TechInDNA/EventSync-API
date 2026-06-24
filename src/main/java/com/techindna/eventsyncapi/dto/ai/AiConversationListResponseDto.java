package com.techindna.eventsyncapi.dto.ai;

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
public class AiConversationListResponseDto {
    private List<AiConversationResponseDto> data;
    private MetaDto meta;
}

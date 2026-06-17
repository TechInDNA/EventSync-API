package com.techindna.eventsyncapi.dto.question;

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
public class QuestionListResponseDto {
    private List<QuestionResponseDto> data;
    private MetaDto meta;
}

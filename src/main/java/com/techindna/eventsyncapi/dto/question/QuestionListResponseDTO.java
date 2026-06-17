package com.techindna.eventsyncapi.dto.question;

import com.techindna.eventsyncapi.dto.MetaDto;
import lombok.*;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class QuestionListResponseDTO {
    private List<QuestionResponseDTO> data;
    private int totalQuestions;
    private int currentPage;
    private int pageSize;
    private MetaDto meta;

}




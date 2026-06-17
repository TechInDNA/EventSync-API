package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.question.QuestionListResponseDTO;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDTO;
import com.techindna.eventsyncapi.entity.Question;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class QuestionMapper {

    public QuestionResponseDTO toResponseDTO(Question question) {
        if (question == null) {
            return null;
        }

        return QuestionResponseDTO.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .session(question.getSession())
                .anonymous(question.isAnonymous())
                .user(question.isAnonymous() ? null : question.getUser())
                .build();
    }

    public QuestionListResponseDTO toListResponseDto(List<Question> questions, long total, int page, int size) {
        if (questions == null) {
            return null;
        }

        List<QuestionResponseDTO> dtos = questions.stream()
                .map(this::toResponseDTO)
                .toList();

        return QuestionListResponseDTO.builder()
                .data(dtos)
                .totalQuestions((int) total)
                .currentPage(page)
                .pageSize(size)
                .meta(null)
                .build();
    }
}
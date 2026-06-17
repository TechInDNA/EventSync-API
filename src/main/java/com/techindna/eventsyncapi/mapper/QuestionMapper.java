package com.techindna.eventsyncapi.mapper;

import com.techindna.eventsyncapi.dto.MetaDto;
import com.techindna.eventsyncapi.dto.auth.ParticipantRefDto;
import com.techindna.eventsyncapi.dto.question.QuestionListResponseDto;
import com.techindna.eventsyncapi.dto.question.QuestionResponseDto;
import com.techindna.eventsyncapi.entity.Question;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QuestionMapper {

    private final UserMapper userMapper;

    public QuestionResponseDto toResponseDto(Question question) {
        if (question == null) return null;

        ParticipantRefDto participant = question.isAnonymous()
                ? null
                : userMapper.toParticipantRef(question.getUser());

        return QuestionResponseDto.builder()
                .id(question.getId())
                .title(question.getTitle())
                .content(question.getContent())
                .createdAt(question.getCreatedAt())
                .sessionId(question.getSession().getId())
                .anonymous(question.isAnonymous())
                .participant(participant)
                .upvotes(0)
                .build();
    }

    public QuestionListResponseDto toListResponseDto(List<Question> questions, long total, int page, int size) {
        List<QuestionResponseDto> data = questions.stream()
                .map(this::toResponseDto)
                .toList();

        MetaDto meta = MetaDto.builder()
                .total(total)
                .page(page)
                .size(size)
                .build();

        return QuestionListResponseDto.builder()
                .data(data)
                .meta(meta)
                .build();
    }
}

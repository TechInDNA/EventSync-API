package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.UnprocessableEntityException;
import com.techindna.eventsyncapi.mapper.SpeakerMapper;
import com.techindna.eventsyncapi.repository.ExternalLinkRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class SpeakerService {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final DataValidator dataValidator;
    private final SpeakerMapper speakerMapper;

    @Transactional
    public SpeakerResponseDto createSpeaker(SpeakerInputDto request) {
        dataValidator.validateName("firstName", request.getFirstName());
        dataValidator.validateName("lastName", request.getLastName());
        dataValidator.validateEmail(request.getEmail());
        dataValidator.validateBio(request.getBio());
        dataValidator.validateUrl("profilePicture", request.getProfilePicture());

        dataValidator.externalLinkValidator(request.getExternalLinks());

        User speaker = userRepository.insertSpeaker(
                request.getFirstName().strip(),
                request.getLastName().strip(),
                request.getEmail().strip(),
                request.getProfilePicture() != null ? request.getProfilePicture().strip() : null,
                request.getBio() != null ? request.getBio().strip() : null
        ).orElseThrow(() -> new ConflictException(
                String.format("Email %s already exists.", request.getEmail())
        ));

        List<ExternalLink> savedLinks = new ArrayList<>();
        if (request.getExternalLinks() != null) {
            List<ExternalLink> entities = request.getExternalLinks().stream()
                    .map(dto -> speakerMapper.toEntity(dto, speaker))
                    .toList();
            savedLinks = externalLinkRepository.saveAll(entities);
        }

        return speakerMapper.toResponseDto(speaker, savedLinks);
    }
}

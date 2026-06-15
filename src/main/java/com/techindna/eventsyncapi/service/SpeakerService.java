package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.mapper.SpeakerMapper;
import com.techindna.eventsyncapi.repository.ExternalLinkRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.validator.DataValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpeakerService {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final DataValidator dataValidator;
    private final SpeakerMapper speakerMapper;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final AuthService authService;

    @Transactional
    public SpeakerResponseDto createSpeaker(SpeakerInputDto request) {
        dataValidator.validateName("firstName", request.getFirstName());
        dataValidator.validateName("lastName", request.getLastName());
        dataValidator.validateEmail(request.getEmail());
        dataValidator.validateText("bio", request.getBio());
        dataValidator.validateUrl("profilePicture", request.getProfilePicture());

        dataValidator.externalLinkValidator(request.getExternalLinks());

        User speaker = userRepository.insertSpeaker(speakerMapper.toEntity(request))
                .orElseThrow(() -> new ConflictException(
                        String.format("Email %s already exists.", request.getEmail())
                ));

        List<ExternalLink> savedLinks = new ArrayList<>();
        if (request.getExternalLinks() != null) {
            for (var dto : request.getExternalLinks()) {
                ExternalLink saved = externalLinkRepository.insertExternalLink(
                        speaker.getId(),
                        dto.getName().strip(),
                        dto.getUrl().strip()
                ).orElseThrow(() -> new ConflictException(
                        String.format("URL %s already exists.", dto.getUrl())
                ));
                savedLinks.add(saved);
            }
        }

        return speakerMapper.toResponseDto(speaker, savedLinks);
    }

    @Transactional(readOnly = true)
    public SpeakerDetailResponseDto getSpeakerById(UUID id, String ipAddress) {
        authService.checkBlacklist(ipAddress);

        User speaker = userRepository.findByIdWithExternalLinks(id)
                .orElseThrow(() -> new NotFoundException(String.format("Speaker %s not found.", id)));

        var sessions = sessionRepository.findBySpeakerId(id).stream()
                .map(sessionMapper::toSpeakerSessionDto)
                .toList();

        return speakerMapper.toDetailResponseDto(speaker, speaker.getExternalLinks(), sessions);
    }
}

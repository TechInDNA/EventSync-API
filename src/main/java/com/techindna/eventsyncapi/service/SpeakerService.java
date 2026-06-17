package com.techindna.eventsyncapi.service;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateResponseDto;
import com.techindna.eventsyncapi.entity.ExternalLink;
import com.techindna.eventsyncapi.entity.User;
import com.techindna.eventsyncapi.exception.ConflictException;
import com.techindna.eventsyncapi.exception.NotFoundException;
import com.techindna.eventsyncapi.mapper.ExternalLinkMapper;
import com.techindna.eventsyncapi.mapper.SessionMapper;
import com.techindna.eventsyncapi.mapper.SpeakerMapper;
import com.techindna.eventsyncapi.repository.ExternalLinkRepository;
import com.techindna.eventsyncapi.repository.SessionRepository;
import com.techindna.eventsyncapi.repository.UserRepository;
import com.techindna.eventsyncapi.validator.ExternalLinkValidator;
import com.techindna.eventsyncapi.validator.SpeakerValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SpeakerService {

    private final UserRepository userRepository;
    private final ExternalLinkRepository externalLinkRepository;
    private final ExternalLinkValidator externalLinkValidator;
    private final SpeakerMapper speakerMapper;
    private final ExternalLinkMapper externalLinkMapper;
    private final SessionRepository sessionRepository;
    private final SessionMapper sessionMapper;
    private final AuthService authService;
    private final SpeakerValidator speakerValidator;
    private static final String UNIQUE_CONSTRAINT_VIOLATION = "23505";

    @Transactional
    public SpeakerResponseDto createSpeaker(SpeakerInputDto request) {
        speakerValidator.validateCreation(request);
        externalLinkValidator.externalLinkValidator(request.getExternalLinks());

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

    @Transactional
    public SpeakerUpdateResponseDto updateSpeaker(UUID id, SpeakerUpdateInputDto request) {
        speakerValidator.validateUpdate(request);

        return speakerMapper.toUpdateResponseDto(updateSpeakerOrThrow(id, request));
    }

    private User updateSpeakerOrThrow(UUID id, SpeakerUpdateInputDto request) {
        try {
            return userRepository.updateSpeakerById(
                    id,
                    request.getFirstName().strip(),
                    request.getLastName().strip(),
                    request.getEmail().strip(),
                    request.getProfilePicture() != null ? request.getProfilePicture().strip() : null,
                    request.getBio() != null ? request.getBio().strip() : null
            ).orElseThrow(() -> new NotFoundException(
                    String.format("Speaker %s not found.", id)));
        } catch (DataIntegrityViolationException e) {
            if (uniqueViolation(e)) {
                throw new ConflictException(
                        "Email '" + request.getEmail().strip() + "' already exists.");
            }
            throw e;
        }
    }

    private static boolean uniqueViolation(DataIntegrityViolationException e) {
        return e.getRootCause() instanceof SQLException sqlEx
                && UNIQUE_CONSTRAINT_VIOLATION.equals(sqlEx.getSQLState());
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

    @Transactional
    public void deleteSpeaker(UUID id) {
        userRepository.deleteSpeakerById(id)
                .orElseThrow(() -> new NotFoundException(String.format("Speaker %s not found.", id)));
    }

    @Transactional
    public List<ExternalLinkDto> addExternalLink(UUID speakerId, ExternalLinkDto request) {
        externalLinkValidator.validateSingleLink(request);

        userRepository.findById(speakerId)
                .orElseThrow(() -> new NotFoundException(
                        String.format("Speaker %s not found.", speakerId)));

        externalLinkRepository.insertExternalLink(
                    speakerId,
                    request.getName().strip(),
                    request.getUrl().strip()
        ).orElseThrow(() -> new ConflictException(
                    String.format("URL %s already exists.", request.getUrl())));

        return externalLinkRepository.findByUserId(speakerId).stream()
                .map(externalLinkMapper::toDto)
                .toList();
    }
}

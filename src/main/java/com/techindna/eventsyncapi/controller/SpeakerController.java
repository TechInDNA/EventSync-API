package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.speaker.ExternalLinkDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerListResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerUpdateResponseDto;
import com.techindna.eventsyncapi.service.SpeakerService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/speakers")
@RequiredArgsConstructor
public class SpeakerController {

    private final SpeakerService speakerService;

    @GetMapping
    public ResponseEntity<SpeakerListResponseDto> getAllSpeakers(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String search,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(speakerService.getAllSpeakers(page, size, search, request.getRemoteAddr()));
    }

    @PostMapping
    public ResponseEntity<SpeakerResponseDto> createSpeaker(@RequestBody SpeakerInputDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speakerService.createSpeaker(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<SpeakerUpdateResponseDto> updateSpeaker(
            @PathVariable UUID id,
            @RequestBody SpeakerUpdateInputDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(speakerService.updateSpeaker(id, request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpeakerDetailResponseDto> getSpeakerById(@PathVariable UUID id, HttpServletRequest  request) {
        return ResponseEntity.status(HttpStatus.OK).body(speakerService.getSpeakerById(id, request.getRemoteAddr()));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSpeaker(@PathVariable UUID id) {
        speakerService.deleteSpeaker(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PostMapping("/{id}/external-link")
    public ResponseEntity<List<ExternalLinkDto>> addExternalLink(
            @PathVariable UUID id,
            @RequestBody ExternalLinkDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(speakerService.addExternalLink(id, request));
    }

    @DeleteMapping("/{id}/external-link")
    public ResponseEntity<Void> deleteExternalLink(
            @PathVariable UUID id,
            @RequestParam UUID externalLinkId
    ) {
        speakerService.deleteExternalLink(id, externalLinkId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

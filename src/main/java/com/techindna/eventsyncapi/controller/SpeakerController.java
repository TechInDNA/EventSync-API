package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.speaker.SpeakerDetailResponseDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerInputDto;
import com.techindna.eventsyncapi.dto.speaker.SpeakerResponseDto;
import com.techindna.eventsyncapi.service.SpeakerService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/speakers")
@RequiredArgsConstructor
public class SpeakerController {

    private final SpeakerService speakerService;

    @PostMapping
    public ResponseEntity<SpeakerResponseDto> createSpeaker(@RequestBody SpeakerInputDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(speakerService.createSpeaker(request));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SpeakerDetailResponseDto> getSpeakerById(@PathVariable UUID id) {
        return ResponseEntity.status(HttpStatus.OK).body(speakerService.getSpeakerById(id));
    }
}

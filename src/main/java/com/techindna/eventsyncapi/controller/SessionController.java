package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.session.SessionDetailResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionInputDto;
import com.techindna.eventsyncapi.dto.session.SessionListResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionResponseDto;
import com.techindna.eventsyncapi.dto.session.SessionSpeakerInputDto;
import com.techindna.eventsyncapi.dto.session.SessionUpdateInputDto;
import com.techindna.eventsyncapi.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@RestController
@RequestMapping("/sessions")
@RequiredArgsConstructor
public class SessionController {

    private final SessionService sessionService;

    @GetMapping
    public ResponseEntity<SessionListResponseDto> getAllSessions(
            @RequestParam(required = false) String room,
            @RequestParam(required = false) String speaker,
            @RequestParam(required = false) Boolean live,
            @RequestParam(required = false) String event,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "20") int size,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(sessionService.getAllSessions(page, size, room, event, speaker, live, request.getRemoteAddr()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<SessionDetailResponseDto> getSessionById(
            @PathVariable UUID id,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(sessionService.getSessionById(id, request.getRemoteAddr()));
    }

    @PostMapping
    public ResponseEntity<SessionResponseDto> createSession(@RequestBody SessionInputDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(sessionService.createSession(request));
    }

    @PostMapping("/{sessionId}/speaker/{speakerId}")
    public ResponseEntity<String> addSpeakerToSession(
            @PathVariable UUID sessionId,
            @PathVariable UUID speakerId,
            @RequestBody SessionSpeakerInputDto request
    ) {
        return ResponseEntity.status(HttpStatus.CREATED).body(
                sessionService.addSpeakerToSession(sessionId, speakerId, request)
        );
    }

    @DeleteMapping("/{sessionId}/speaker/{speakerId}")
    public ResponseEntity<Void> deleteSpeakerFromSession(
            @PathVariable UUID sessionId,
            @PathVariable UUID speakerId
    ) {
        sessionService.deleteSpeakerFromSession(sessionId, speakerId);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSession(@PathVariable UUID id) {
        sessionService.deleteSession(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }

    @PutMapping("/{id}")
    public ResponseEntity<SessionResponseDto> updateSession(
            @PathVariable UUID id,
            @RequestBody SessionUpdateInputDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(sessionService.updateSession(id, request));
    }
}

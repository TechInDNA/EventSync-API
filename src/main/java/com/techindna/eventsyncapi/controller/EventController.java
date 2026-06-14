package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.event.EventDetailResponseDto;
import com.techindna.eventsyncapi.dto.event.EventInputDto;
import com.techindna.eventsyncapi.dto.event.EventListResponseDto;
import com.techindna.eventsyncapi.dto.event.EventResponseDto;
import com.techindna.eventsyncapi.service.EventService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.Instant;
import java.util.UUID;

@RestController
@RequestMapping("/events")
@RequiredArgsConstructor
public class EventController {

    private final EventService eventService;

    @GetMapping
    public ResponseEntity<EventListResponseDto> getAllEvents(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) Instant startDate,
            @RequestParam(required = false) Instant endDate,
            @RequestParam(required = false) Boolean isLive,
            HttpServletRequest request
    ) {
        return ResponseEntity.status(HttpStatus.OK)
                .body(eventService.getAllEvents(page, size, title, location, startDate, endDate, isLive, request.getRemoteAddr()));
    }

    @PostMapping
    public ResponseEntity<EventDetailResponseDto> createEvent(@RequestBody EventInputDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(eventService.createEvent(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<EventDetailResponseDto> updateEvent(
            @PathVariable UUID id,
            @RequestBody EventInputDto request
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(eventService.updateEvent(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEvent(@PathVariable UUID id) {
        eventService.deleteEvent(id);
        return ResponseEntity.status(HttpStatus.NO_CONTENT).build();
    }
}

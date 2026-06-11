package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.RoomInputDto;
import com.techindna.eventsyncapi.dto.RoomListResponseDto;
import com.techindna.eventsyncapi.dto.RoomResponseDto;
import com.techindna.eventsyncapi.service.RoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/rooms")
@RequiredArgsConstructor
public class RoomController {

    private final RoomService roomService;

    @GetMapping
    public ResponseEntity<RoomListResponseDto> getAllRooms(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size
    ) {
        return ResponseEntity.status(HttpStatus.OK).body(roomService.getAllRooms(page, size));
    }

    @PostMapping
    public ResponseEntity<RoomResponseDto> createRoom(@Valid @RequestBody RoomInputDto request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(roomService.createRoom(request));
    }
}

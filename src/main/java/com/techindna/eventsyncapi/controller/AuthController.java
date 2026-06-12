package com.techindna.eventsyncapi.controller;

import com.techindna.eventsyncapi.dto.auth.*;
import com.techindna.eventsyncapi.service.AuthService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private static final int COOKIE_MAX_AGE = 43200;
    private final AuthService authService;

    @PostMapping("/login")
    public ResponseEntity<AuthLoginResponseDto> login(
            @Valid @RequestBody AuthLoginRequestDto request,
            HttpServletRequest servletRequest
    ) {
        String ipAddress = servletRequest.getRemoteAddr();
        String userAgent = servletRequest.getHeader("User-Agent");
        AuthLoginResponseDto response = authService.login(request, ipAddress, userAgent);

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", response.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite("Strict")
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }

    @PostMapping("/participant")
    public ResponseEntity<AuthParticipantResponseDto> participate(
            @RequestBody AuthParticipantRequestDto request,
            HttpServletRequest servletRequest
    ) {
        AuthParticipantResponseDto response = authService.participate(request, servletRequest.getRemoteAddr());

        ResponseCookie jwtCookie = ResponseCookie.from("jwt", response.getToken())
                .httpOnly(true)
                .secure(true)
                .path("/")
                .maxAge(COOKIE_MAX_AGE)
                .sameSite("Strict")
                .build();

        return ResponseEntity.status(HttpStatus.OK)
                .header(HttpHeaders.SET_COOKIE, jwtCookie.toString())
                .body(response);
    }
}

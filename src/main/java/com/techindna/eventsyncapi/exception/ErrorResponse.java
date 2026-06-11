package com.techindna.eventsyncapi.exception;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public record ErrorResponse(
        int status,
        String error,
        String message
) {

    public static void send(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value());
        response.getWriter().write(
                "{\"status\":%d,\"error\":\"%s\",\"message\":\"%s\"}".formatted(
                        status.value(),
                        status.getReasonPhrase(),
                        message
                )
        );
    }
}

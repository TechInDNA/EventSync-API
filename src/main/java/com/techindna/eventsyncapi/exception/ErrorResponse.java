package com.techindna.eventsyncapi.exception;

import tools.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;

import java.io.IOException;

public record ErrorResponse(
        int status,
        String error,
        String message
) {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    public static void send(HttpServletResponse response, HttpStatus status, String message) throws IOException {
        response.setContentType("application/json;charset=UTF-8");
        response.setStatus(status.value());
        MAPPER.writeValue(response.getWriter(), new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message
        ));
    }
}

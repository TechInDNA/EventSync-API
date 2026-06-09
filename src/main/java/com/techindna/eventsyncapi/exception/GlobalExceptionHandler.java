package com.techindna.eventsyncapi.exception;

import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // ── Validation: 422 Unprocessable Entity ──

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidationErrors(MethodArgumentNotValidException ex, HttpServletRequest request) {
        var details = ex.getBindingResult().getFieldErrors().stream()
                .map(f -> new ErrorResponse.FieldDetail(f.getField(), f.getDefaultMessage()))
                .collect(Collectors.toList());
        return respond(HttpStatus.UNPROCESSABLE_ENTITY, "Validation failed", details, request);
    }

    // ── 400 Bad Request ──

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleMalformedBody(HttpMessageNotReadableException ex, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Malformed request body", request);
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Required parameter '%s' is missing".formatted(ex.getParameterName()), request);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex, HttpServletRequest request) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid value for parameter '%s'".formatted(ex.getName()), request);
    }

    // ── 401 Unauthorized ──

    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<ErrorResponse> handleUnauthorized(UnauthorizedException ex, HttpServletRequest request) {
        return respond(HttpStatus.UNAUTHORIZED, ex.getMessage(), request);
    }

    // ── 403 Forbidden ──

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDenied(AccessDeniedException ex, HttpServletRequest request) {
        return respond(HttpStatus.FORBIDDEN, "Insufficient privileges", request);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthentication(AuthenticationException ex, HttpServletRequest request) {
        return respond(HttpStatus.UNAUTHORIZED, "Authentication failed", request);
    }

    // ── 404 Not Found ──

    @ExceptionHandler(NotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(NotFoundException ex, HttpServletRequest request) {
        return respond(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // ── 409 Conflict ──

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex, HttpServletRequest request) {
        String message = "Data integrity violation";
        String cause = ex.getMostSpecificCause().getMessage();
        if (cause != null && cause.contains("unique")) {
            message = "A record with the same unique value already exists";
        }
        return respond(HttpStatus.CONFLICT, message, request);
    }

    // ── 429 Too Many Requests ──

    @ExceptionHandler(TooManyRequestException.class)
    public ResponseEntity<ErrorResponse> handleTooManyRequests(TooManyRequestException ex, HttpServletRequest request) {
        return respond(HttpStatus.TOO_MANY_REQUESTS, ex.getMessage(), request);
    }

    // ── 500 Internal Server Error ──

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleFallback(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} {}", request.getMethod(), request.getRequestURI(), ex);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "An unexpected error occurred", request);
    }

    // ── Helpers ──

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, HttpServletRequest request) {
        return respond(status, message, List.of(), request);
    }

    private ResponseEntity<ErrorResponse> respond(HttpStatus status, String message, List<ErrorResponse.FieldDetail> details, HttpServletRequest request) {
        var body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                details.isEmpty() ? null : details,
                request.getRequestURI(),
                LocalDateTime.now()
        );
        return new ResponseEntity<>(body, status);
    }

    public record ErrorResponse(
            int status,
            String error,
            String message,
            List<FieldDetail> details,
            String path,
            LocalDateTime timestamp
    ) {
        public record FieldDetail(String field, String message) {}
    }
}

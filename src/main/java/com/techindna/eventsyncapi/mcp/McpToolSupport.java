package com.techindna.eventsyncapi.mcp;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.time.Instant;
import java.time.format.DateTimeParseException;
import java.util.UUID;
import java.util.function.Supplier;


final class McpToolSupport {

    private McpToolSupport() {
    }

    static UUID parseUuid(String value) {
        try {
            return UUID.fromString(value);
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new ToolInputException(String.format("invalid UUID: '%s'", value));
        }
    }

    static Instant parseInstant(String value) {
        try {
            return Instant.parse(value);
        } catch (DateTimeParseException | NullPointerException e) {
            throw new ToolInputException(String.format(
                    "invalid ISO-8601 instant: '%s' (expected e.g. 2026-07-15T09:00:00Z)", value));
        }
    }

    static String run(Logger log, String toolName, Supplier<String> body) {
        try {
            return body.get();
        } catch (ToolInputException e) {
            log.warn("Tool {} rejected input: {}", toolName, e.getMessage());
            return String.format("Operation failed: %s", e.getMessage());
        } catch (Exception e) {
            log.warn("Tool {} failed: {}", toolName, e.getMessage(), e);
            return String.format("Operation failed: %s", e.getMessage());
        }
    }

    static int pageOrDefault(Integer page) {
        return page != null ? page : 1;
    }

    static int sizeOrDefault(Integer size) {
        return size != null ? size : 10;
    }

    static int intOrDefault(Integer value, int fallback) {
        return value != null ? value : fallback;
    }

    static Logger logger(Class<?> owner) {
        return LoggerFactory.getLogger(owner);
    }

    private static final class ToolInputException extends RuntimeException {
        ToolInputException(String message) {
            super(message);
        }
    }
}

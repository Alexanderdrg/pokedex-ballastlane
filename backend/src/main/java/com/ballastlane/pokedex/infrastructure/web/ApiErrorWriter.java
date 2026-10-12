package com.ballastlane.pokedex.infrastructure.web;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.time.Clock;
import java.util.Map;

/**
 * Writes {@link ApiError} bodies from places that run before Spring MVC (security filters).
 */
@Component
public class ApiErrorWriter {

    private final ObjectMapper objectMapper;
    private final Clock clock;

    public ApiErrorWriter(ObjectMapper objectMapper, Clock clock) {
        this.objectMapper = objectMapper;
        this.clock = clock;
    }

    public ApiError build(HttpStatus status, String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(clock.instant(), status.value(), status.getReasonPhrase(), message, path, fieldErrors);
    }

    public void write(HttpServletRequest request, HttpServletResponse response, HttpStatus status, String message)
            throws IOException {
        response.setStatus(status.value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), build(status, message, request.getRequestURI(), Map.of()));
    }
}

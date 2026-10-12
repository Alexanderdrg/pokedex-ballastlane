package com.ballastlane.pokedex.infrastructure.web;

import com.ballastlane.pokedex.TestData;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Path;
import org.junit.jupiter.api.Test;
import org.springframework.context.MessageSourceResolvable;
import org.springframework.core.MethodParameter;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.method.ParameterValidationResult;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.HandlerMethodValidationException;

import java.time.Clock;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/**
 * Failures that are hard to provoke through HTTP (database races, framework validation)
 * still have to honour the error contract.
 */
class GlobalExceptionHandlerTest {

    private final GlobalExceptionHandler handler = new GlobalExceptionHandler(
            new ApiErrorWriter(new ObjectMapper(), Clock.fixed(TestData.NOW, ZoneOffset.UTC)));
    private final MockHttpServletRequest request = new MockHttpServletRequest("PUT", "/api/local-pokemon/1");

    @Test
    void uniqueConstraintViolationIs409() {
        var response = handler.handleIntegrity(new DataIntegrityViolationException("duplicate key"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).isEqualTo("The resource conflicts with existing data");
        assertThat(response.getBody().path()).isEqualTo("/api/local-pokemon/1");
        assertThat(response.getBody().timestamp()).isEqualTo(TestData.NOW);
    }

    @Test
    void concurrentModificationIs409() {
        var response = handler.handleOptimisticLock(new OptimisticLockingFailureException("stale"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody().message()).contains("modified concurrently");
    }

    @Test
    void missingRequestParameterIs400WithFieldError() {
        var response = handler.handleMissingParam(new MissingServletRequestParameterException("page", "int"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().fieldErrors()).containsEntry("page", "is required");
    }

    @Test
    void constraintViolationIs400WithFieldError() {
        ConstraintViolation<?> violation = mock(ConstraintViolation.class);
        Path path = mock(Path.class);
        when(path.toString()).thenReturn("size");
        when(violation.getPropertyPath()).thenReturn(path);
        when(violation.getMessage()).thenReturn("must be less than or equal to 100");

        var response = handler.handleConstraint(new ConstraintViolationException(Set.of(violation)), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().fieldErrors()).containsEntry("size", "must be less than or equal to 100");
    }

    @Test
    void methodParameterValidationIs400WithFieldError() {
        MethodParameter parameter = mock(MethodParameter.class);
        when(parameter.getParameterName()).thenReturn("size");
        MessageSourceResolvable error = mock(MessageSourceResolvable.class);
        when(error.getDefaultMessage()).thenReturn("must be positive");
        ParameterValidationResult result = mock(ParameterValidationResult.class);
        when(result.getMethodParameter()).thenReturn(parameter);
        when(result.getResolvableErrors()).thenReturn(List.of(error));
        HandlerMethodValidationException exception = mock(HandlerMethodValidationException.class);
        when(exception.getAllValidationResults()).thenReturn(List.of(result));

        var response = handler.handleParamValidation(exception, request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody().fieldErrors()).containsEntry("size", "must be positive");
    }

    @Test
    void unexpectedErrorIs500WithoutLeakingDetails() {
        var response = handler.handleUnexpected(new IllegalStateException("db password is hunter2"), request);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody().message()).isEqualTo("Unexpected error");
    }
}

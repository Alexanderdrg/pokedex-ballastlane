package com.ballastlane.pokedex.infrastructure.web;

import com.ballastlane.pokedex.domain.exception.ConflictException;
import com.ballastlane.pokedex.domain.exception.ExternalServiceException;
import com.ballastlane.pokedex.domain.exception.InvalidCredentialsException;
import com.ballastlane.pokedex.domain.exception.NotFoundException;
import com.ballastlane.pokedex.domain.exception.StaleVersionException;
import com.ballastlane.pokedex.domain.exception.ValidationException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.dao.OptimisticLockingFailureException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.HttpMediaTypeNotSupportedException;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Maps every failure to the {@link ApiError} contract with the right HTTP status.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final ApiErrorWriter errors;

    public GlobalExceptionHandler(ApiErrorWriter errors) {
        this.errors = errors;
    }

    // ---- 400
    @ExceptionHandler(ValidationException.class)
    ResponseEntity<ApiError> handleDomainValidation(ValidationException e, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage(), req, e.getFieldErrors());
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    ResponseEntity<ApiError> handleBodyValidation(MethodArgumentNotValidException e, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getBindingResult().getFieldErrors().forEach(f -> fields.putIfAbsent(f.getField(), f.getDefaultMessage()));
        return respond(HttpStatus.BAD_REQUEST, "Request validation failed", req, fields);
    }

    @ExceptionHandler(HandlerMethodValidationException.class)
    ResponseEntity<ApiError> handleParamValidation(HandlerMethodValidationException e, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getAllValidationResults().forEach(r -> r.getResolvableErrors().forEach(err ->
                fields.putIfAbsent(r.getMethodParameter().getParameterName(), err.getDefaultMessage())));
        return respond(HttpStatus.BAD_REQUEST, "Request validation failed", req, fields);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    ResponseEntity<ApiError> handleConstraint(ConstraintViolationException e, HttpServletRequest req) {
        Map<String, String> fields = new LinkedHashMap<>();
        e.getConstraintViolations().forEach(v -> fields.putIfAbsent(v.getPropertyPath().toString(), v.getMessage()));
        return respond(HttpStatus.BAD_REQUEST, "Request validation failed", req, fields);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    ResponseEntity<ApiError> handleUnreadable(HttpMessageNotReadableException e, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "Malformed JSON request body", req, Map.of());
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    ResponseEntity<ApiError> handleTypeMismatch(MethodArgumentTypeMismatchException e, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, "Invalid value for parameter '" + e.getName() + "'", req,
                Map.of(e.getName(), "invalid value"));
    }

    @ExceptionHandler(MissingServletRequestParameterException.class)
    ResponseEntity<ApiError> handleMissingParam(MissingServletRequestParameterException e, HttpServletRequest req) {
        return respond(HttpStatus.BAD_REQUEST, e.getMessage(), req, Map.of(e.getParameterName(), "is required"));
    }

    // ---- 401 / 404 / 405 / 409 / 412 / 415 / 428
    @ExceptionHandler(InvalidCredentialsException.class)
    ResponseEntity<ApiError> handleCredentials(InvalidCredentialsException e, HttpServletRequest req) {
        return respond(HttpStatus.UNAUTHORIZED, e.getMessage(), req, Map.of());
    }

    @ExceptionHandler(NotFoundException.class)
    ResponseEntity<ApiError> handleNotFound(NotFoundException e, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, e.getMessage(), req, Map.of());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    ResponseEntity<ApiError> handleNoResource(NoResourceFoundException e, HttpServletRequest req) {
        return respond(HttpStatus.NOT_FOUND, "Resource not found", req, Map.of());
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    ResponseEntity<ApiError> handleMethod(HttpRequestMethodNotSupportedException e, HttpServletRequest req) {
        return respond(HttpStatus.METHOD_NOT_ALLOWED, e.getMessage(), req, Map.of());
    }

    @ExceptionHandler(HttpMediaTypeNotSupportedException.class)
    ResponseEntity<ApiError> handleMediaType(HttpMediaTypeNotSupportedException e, HttpServletRequest req) {
        return respond(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "Content type must be application/json", req, Map.of());
    }

    @ExceptionHandler(ConflictException.class)
    ResponseEntity<ApiError> handleConflict(ConflictException e, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, e.getMessage(), req, Map.of());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    ResponseEntity<ApiError> handleIntegrity(DataIntegrityViolationException e, HttpServletRequest req) {
        // e.g. two concurrent imports of the same Pokemon hitting the unique constraint
        return respond(HttpStatus.CONFLICT, "The resource conflicts with existing data", req, Map.of());
    }

    @ExceptionHandler(OptimisticLockingFailureException.class)
    ResponseEntity<ApiError> handleOptimisticLock(OptimisticLockingFailureException e, HttpServletRequest req) {
        return respond(HttpStatus.CONFLICT, "The resource was modified concurrently, please retry", req, Map.of());
    }

    @ExceptionHandler(StaleVersionException.class)
    ResponseEntity<ApiError> handleStaleVersion(StaleVersionException e, HttpServletRequest req) {
        return respond(HttpStatus.PRECONDITION_FAILED, e.getMessage(), req, Map.of());
    }

    @ExceptionHandler(MissingRequestHeaderException.class)
    ResponseEntity<ApiError> handleMissingHeader(MissingRequestHeaderException e, HttpServletRequest req) {
        if (ETags.IF_MATCH.equalsIgnoreCase(e.getHeaderName())) {
            // RFC 6585: the server requires a conditional request to prevent lost updates
            return respond(HttpStatus.PRECONDITION_REQUIRED,
                    "If-Match header is required: send the ETag you read to avoid overwriting newer changes", req,
                    Map.of(ETags.IF_MATCH, "is required"));
        }
        return respond(HttpStatus.BAD_REQUEST, e.getMessage(), req, Map.of(e.getHeaderName(), "is required"));
    }

    // ---- 5xx
    @ExceptionHandler(ExternalServiceException.class)
    ResponseEntity<ApiError> handleUpstream(ExternalServiceException e, HttpServletRequest req) {
        log.warn("Upstream failure on {}: {}", req.getRequestURI(), e.getMessage(), e.getCause());
        return respond(HttpStatus.BAD_GATEWAY, e.getMessage(), req, Map.of());
    }

    @ExceptionHandler(Exception.class)
    ResponseEntity<ApiError> handleUnexpected(Exception e, HttpServletRequest req) {
        log.error("Unexpected error on {}", req.getRequestURI(), e);
        return respond(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected error", req, Map.of());
    }

    private ResponseEntity<ApiError> respond(HttpStatus status, String message, HttpServletRequest req,
                                             Map<String, String> fieldErrors) {
        return ResponseEntity.status(status).body(errors.build(status, message, req.getRequestURI(), fieldErrors));
    }
}

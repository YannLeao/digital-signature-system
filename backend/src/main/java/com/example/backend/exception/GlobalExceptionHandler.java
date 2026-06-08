package com.example.backend.exception;

import com.example.backend.dto.ApiErrorResponse;
import com.example.backend.dto.FieldErrorResponse;
import jakarta.validation.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.resource.NoResourceFoundException;

import java.time.Clock;
import java.time.Instant;
import java.util.Comparator;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private final Clock clock;

    public GlobalExceptionHandler() {
        this(Clock.systemUTC());
    }

    GlobalExceptionHandler(Clock clock) {
        this.clock = clock;
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiErrorResponse> handleMethodArgumentNotValid(MethodArgumentNotValidException exception) {
        List<FieldErrorResponse> fields = exception.getBindingResult()
                .getFieldErrors()
                .stream()
                .sorted(Comparator.comparing(FieldError::getField))
                .map(error -> new FieldErrorResponse(error.getField(), resolveFieldMessage(error)))
                .toList();

        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VAL_001, fields);
    }

    @ExceptionHandler({
            ConstraintViolationException.class,
            MissingServletRequestParameterException.class,
            MethodArgumentTypeMismatchException.class
    })
    public ResponseEntity<ApiErrorResponse> handleInvalidParameter(Exception exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.VAL_002);
    }

    @ExceptionHandler(ApiException.class)
    public ResponseEntity<ApiErrorResponse> handleApiException(ApiException exception) {
        return buildResponse(exception.status(), exception.code(), exception.getMessage());
    }

    @ExceptionHandler(PdfValidationException.class)
    public ResponseEntity<ApiErrorResponse> handlePdfValidation(PdfValidationException exception) {
        return buildResponse(HttpStatus.BAD_REQUEST, ApiErrorCode.DOC_001, exception.getMessage());
    }

    @ExceptionHandler(NoResourceFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleNoResourceFound(NoResourceFoundException exception) {
        return buildResponse(HttpStatus.NOT_FOUND, ApiErrorCode.SYS_002);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiErrorResponse> handleUnexpectedException(Exception exception) {
        LOGGER.error("Unexpected API error", exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, ApiErrorCode.SYS_001);
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, ApiErrorCode code) {
        return buildResponse(status, code, code.defaultMessage());
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(HttpStatus status, ApiErrorCode code, String message) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.of(code.name(), message, Instant.now(clock)));
    }

    private ResponseEntity<ApiErrorResponse> buildResponse(
            HttpStatus status,
            ApiErrorCode code,
            List<FieldErrorResponse> fields
    ) {
        return ResponseEntity.status(status)
                .body(ApiErrorResponse.withFields(code.name(), code.defaultMessage(), Instant.now(clock), fields));
    }

    private String resolveFieldMessage(FieldError error) {
        String defaultMessage = error.getDefaultMessage();

        if (defaultMessage == null || defaultMessage.isBlank()) {
            return "Campo invalido.";
        }

        return defaultMessage;
    }
}

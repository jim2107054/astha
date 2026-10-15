package com.astha.platform.problem;

import com.astha.platform.idempotency.IdempotencyConflictException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.net.URI;
import java.util.ArrayList;
import java.util.List;

@RestControllerAdvice
public class GlobalExceptionHandler {
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ProblemDetails> handleValidationException(MethodArgumentNotValidException ex, HttpServletRequest request) {
        ProblemDetails pd = ProblemDetails.forStatus(
            HttpStatus.BAD_REQUEST.value(),
            "Validation Failed",
            "One or more request parameters failed validation"
        );
        pd.setInstance(URI.create(request.getRequestURI()));

        List<ProblemDetails.InvalidParam> invalidParams = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            invalidParams.add(new ProblemDetails.InvalidParam(fe.getField(), fe.getDefaultMessage()));
        }
        pd.setInvalidParams(invalidParams);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(IdempotencyConflictException.class)
    public ResponseEntity<ProblemDetails> handleIdempotencyConflict(IdempotencyConflictException ex, HttpServletRequest request) {
        log.warn("Idempotency conflict detected for key: {}", ex.getKey());
        ProblemDetails pd = ProblemDetails.forStatus(
            HttpStatus.UNPROCESSABLE_ENTITY.value(),
            "Idempotency Key Conflict",
            ex.getMessage()
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(pd);
    }

    @ExceptionHandler(SecurityException.class)
    public ResponseEntity<ProblemDetails> handleSecurityException(SecurityException ex, HttpServletRequest request) {
        ProblemDetails pd = ProblemDetails.forStatus(
            HttpStatus.FORBIDDEN.value(),
            "Access Denied",
            ex.getMessage()
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(pd);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ProblemDetails> handleIllegalArgument(IllegalArgumentException ex, HttpServletRequest request) {
        ProblemDetails pd = ProblemDetails.forStatus(
            HttpStatus.BAD_REQUEST.value(),
            "Invalid Request Argument",
            ex.getMessage()
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(pd);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ProblemDetails> handleGeneralException(Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception processing request: {}", request.getRequestURI(), ex);
        ProblemDetails pd = ProblemDetails.forStatus(
            HttpStatus.INTERNAL_SERVER_ERROR.value(),
            "Internal Server Error",
            "An unexpected error occurred while processing the request"
        );
        pd.setInstance(URI.create(request.getRequestURI()));
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(pd);
    }
}

package com.cognizant.healthcaregov.exception;

import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {


    @ExceptionHandler(AppException.class)
    public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
        log.error("AppException [{}]: {}", ex.getStatus(), ex.getMessage());
        return build(ex.getStatus().value(), ex.getMessage(), ex.getStatus());
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex) {
        String msg = "Database constraint violation: a record with these values already exists.";

        if (ex.getMostSpecificCause() != null) {
            String detail = ex.getMostSpecificCause().getMessage();
            if (detail != null) {
                if (detail.contains("email")) {
                    msg = "Email address is already registered.";
                } else if (detail.contains("uk_schedule") || detail.contains("schedule")) {
                    msg = "A schedule slot already exists for this doctor on this date and time.";
                } else if (detail.contains("Cannot delete") || detail.contains("foreign key")) {
                    msg = "Cannot delete: this record is referenced by other data (appointments, resources, etc.).";
                }
            }
        }
        log.error("DataIntegrityViolation: {}", ex.getMostSpecificCause() != null
                ? ex.getMostSpecificCause().getMessage() : ex.getMessage());
        return build(HttpStatus.CONFLICT.value(), msg, HttpStatus.CONFLICT);
    }


    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex) {
        String msg = ex.getBindingResult().getFieldErrors().stream()
                .map(e -> e.getField() + ": " + e.getDefaultMessage())
                .findFirst()
                .orElse("Validation error");
        log.error("Validation failed: {}", msg);
        return build(HttpStatus.BAD_REQUEST.value(), msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {

        String typeName = (ex.getRequiredType() != null)
                ? ex.getRequiredType().getSimpleName()
                : "unknown";
        String msg = "Parameter '" + ex.getName() + "' must be of type " + typeName;
        return build(HttpStatus.BAD_REQUEST.value(), msg, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex) {
        log.error("Unhandled exception: {}", ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Internal server error: " + ex.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR);
    }

    private ResponseEntity<ErrorResponse> build(int code, String msg, HttpStatus status) {
        return new ResponseEntity<>(new ErrorResponse(code, msg, System.currentTimeMillis()), status);
    }
}

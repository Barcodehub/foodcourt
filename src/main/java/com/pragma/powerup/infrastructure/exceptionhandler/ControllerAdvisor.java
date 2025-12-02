package com.pragma.powerup.infrastructure.exceptionhandler;

import com.pragma.powerup.domain.exception.*;
import com.pragma.powerup.infrastructure.exception.NoDataFoundException;
import jakarta.validation.ConstraintViolationException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class ControllerAdvisor {

    private static final String MESSAGE = "message";
    private static final String TIMESTAMP = "timestamp";
    private static final String STATUS = "status";
    private static final String ERROR = "error";

    @ExceptionHandler(NoDataFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoDataFoundException(
            NoDataFoundException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, HttpStatus.NOT_FOUND.getReasonPhrase());
        response.put(MESSAGE, ExceptionResponse.NO_DATA_FOUND.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UserUnderageException.class)
    public ResponseEntity<Map<String, Object>> handleUserUnderageException(
            UserUnderageException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "User Underage");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(RestaurantAlreadyExistsException.class)
    public ResponseEntity<Map<String, Object>> handleRestaurantAlreadyExistsException(
            RestaurantAlreadyExistsException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.CONFLICT.value());
        response.put(ERROR, "Restaurant Already Exists");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(InvalidRestaurantException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidRestaurantException(
            InvalidRestaurantException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Invalid Restaurant");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(UserNotOwnerException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotOwnerException(
            UserNotOwnerException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.FORBIDDEN.value());
        response.put(ERROR, "User Not Owner");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleUserNotFoundException(
            UserNotFoundException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, "User Not Found");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(InvalidDishException.class)
    public ResponseEntity<Map<String, Object>> handleInvalidDishException(
            InvalidDishException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Invalid Dish");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(DishNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleDishNotFoundException(
            DishNotFoundException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, "Dish Not Found");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(RestaurantNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleRestaurantNotFoundException(
            RestaurantNotFoundException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.NOT_FOUND.value());
        response.put(ERROR, "Restaurant Not Found");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(response);
    }

    @ExceptionHandler(UnauthorizedOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedOperationException(
            UnauthorizedOperationException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.FORBIDDEN.value());
        response.put(ERROR, "Unauthorized Operation");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(UnauthorizedRestaurantCreationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedRestaurantCreationException(
            UnauthorizedRestaurantCreationException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.FORBIDDEN.value());
        response.put(ERROR, "Unauthorized Restaurant Creation");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    @ResponseBody
    public ResponseEntity<Object> handleHttpMessageNotReadableException(HttpMessageNotReadableException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Bad Request");
        body.put("message", "Error de formato o valor inválido en el cuerpo de la solicitud. Detalle: " + ex.getMostSpecificCause().getMessage());
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.BAD_REQUEST.value());
        return new ResponseEntity<>(body, HttpStatus.BAD_REQUEST);
    }


    @ExceptionHandler(AccessDeniedException.class)
    @ResponseBody
    public ResponseEntity<Object> handleAccessDeniedException(AccessDeniedException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("error", "Forbidden");
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.FORBIDDEN.value());
        return new ResponseEntity<>(body, HttpStatus.FORBIDDEN);
    }

    @ExceptionHandler(UnauthorizedDishOperationException.class)
    public ResponseEntity<Map<String, Object>> handleUnauthorizedDishOperationException(
            UnauthorizedDishOperationException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.FORBIDDEN.value());
        response.put(ERROR, "Unauthorized Dish Operation");
        response.put(MESSAGE, exception.getMessage());

        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(response);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<Map<String, Object>> handleDataIntegrityViolationException(
            DataIntegrityViolationException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.CONFLICT.value());
        response.put(ERROR, "Data Integrity Violation");

        String message = "Error de integridad de datos";
        String exceptionMessage = exception.getMessage();

        if (exceptionMessage != null) {
            if (exceptionMessage.contains("email")) {
                message = ExceptionResponse.EMAIL_ALREADY_EXISTS.getMessage();
            } else if (exceptionMessage.contains("identification_document")) {
                message = ExceptionResponse.IDENTIFICATION_ALREADY_EXISTS.getMessage();
            } else if (exceptionMessage.contains("nit")) {
                message = "Ya existe un restaurante con este NIT";
            }
        }

        response.put(MESSAGE, message);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(response);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidationExceptions(
            MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Validation Failed");
        response.put(MESSAGE, ExceptionResponse.VALIDATION_FAILED.getMessage());
        response.put("errors", errors);

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<Map<String, Object>> handleConstraintViolationException(ConstraintViolationException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Validation Error");
        response.put(MESSAGE, ExceptionResponse.VALIDATION_FAILED.getMessage());
        response.put("violations", exception.getConstraintViolations().stream()
                .map(v -> Map.of(
                        "property", v.getPropertyPath().toString(),
                        "invalidValue", v.getInvalidValue(),
                        "message", v.getMessage()
                )).toList());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGlobalException(Exception exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.INTERNAL_SERVER_ERROR.value());
        response.put(ERROR, HttpStatus.INTERNAL_SERVER_ERROR.getReasonPhrase());
        response.put(MESSAGE, ExceptionResponse.INTERNAL_SERVER_ERROR.getMessage());

        // Log the actual exception for debugging
        exception.printStackTrace();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(response);
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArgumentException(IllegalArgumentException exception) {
        Map<String, Object> response = new HashMap<>();
        response.put(TIMESTAMP, LocalDateTime.now());
        response.put(STATUS, HttpStatus.BAD_REQUEST.value());
        response.put(ERROR, "Bad Request");
        response.put(MESSAGE, exception.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }
}

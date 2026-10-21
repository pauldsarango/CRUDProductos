package com.productos.crud.exception;

import com.productos.crud.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.method.ParameterErrors;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.HandlerMethodValidationException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.time.Instant;
import java.util.*;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // Excepciones de entrada
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(
            MethodArgumentTypeMismatchException exception
    ) {
        String expected = Optional.ofNullable(exception.getRequiredType())
                .map(Class::getSimpleName)
                .orElse("valor válido");

        String message = String.format(
                "Parameter '%s' has an invalid value: '%s'. Expected type is '%s'",
                exception.getName(),
                exception.getValue(),
                expected
        );

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                message,
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }
    @ExceptionHandler(HandlerMethodValidationException.class)
    public ResponseEntity<ErrorResponse> handleMethodValidation(
            HandlerMethodValidationException exception
    ) {
        Map<String, List<String>> errors = new LinkedHashMap<>();
        exception.getParameterValidationResults().forEach(result -> {
            if (result instanceof ParameterErrors pe) {
                // @Valid sobre un DTO: usamos el nombre del campo
                pe.getFieldErrors().forEach(fe ->
                        errors.computeIfAbsent(fe.getField(), k -> new ArrayList<>())
                                .add(fe.getDefaultMessage()));
            } else {
                // @RequestParam / @PathVariable con constraints: usamos el nombre del parámetro
                String param = result.getMethodParameter().getParameterName();
                result.getResolvableErrors().forEach(e ->
                        errors.computeIfAbsent(param, k -> new ArrayList<>())
                                .add(e.getDefaultMessage()));
            }
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Invalid value for parameter",
                Instant.now(),
                errors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Excepciones de validación DTO
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
            MethodArgumentNotValidException exception)
    {
        Map<String, List<String>> fieldErrors = new LinkedHashMap<>();
        // Capturamos el mensaje del error y evitamos NPE
        exception.getBindingResult().getFieldErrors().forEach(fe -> {
            String message = fe.isBindingFailure()
                    ? "Parameter " + fe.getField() + " has an invalid value: " + fe.getRejectedValue()
                    : fe.getDefaultMessage();
            fieldErrors.computeIfAbsent(fe.getField(), k -> new ArrayList<>()).add(message);
        });

        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Validation Failed",
                Instant.now(),
                fieldErrors
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
    }

    // Excepciones de lógica
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFoundException(
            ResourceNotFoundException exception
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.NOT_FOUND.value(),
                HttpStatus.NOT_FOUND.getReasonPhrase(),
                exception.getMessage(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(errorResponse);
    }

    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicateResourceException(
            DuplicateResourceException exception
    ){
        ErrorResponse errorResponse = new ErrorResponse(
                HttpStatus.CONFLICT.value(),
                HttpStatus.CONFLICT.getReasonPhrase(),
                exception.getMessage(),
                Instant.now(),
                null
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(errorResponse);
    }


    // CAPTURADOR DE UNIQUE EN DATAINTEGRITYVIOLATION
    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    private static final Map<String, String> CONSTRAINT_MESSAGES = Map.of(
            "uk_product_sku", "A product with this SKU already exists"
    );

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrityViolation(
            DataIntegrityViolationException exception
    ) {
        String constraint = findConstraintName(exception);

        if (constraint != null) {
            for (Map.Entry<String, String> entry : CONSTRAINT_MESSAGES.entrySet()) {
                if (constraint.contains(entry.getKey())) {
                    return buildResponse(HttpStatus.CONFLICT, entry.getValue());
                }
            }
        }

        log.error("Unhandled data integrity violation (constraint={})", constraint, exception);
        return buildResponse(HttpStatus.INTERNAL_SERVER_ERROR, "Unexpected data integrity error");
    }

    private String findConstraintName(Throwable throwable) {
        Throwable current = throwable;
        while (current != null) {
            if (current instanceof org.hibernate.exception.ConstraintViolationException cve
                    && cve.getConstraintName() != null) {
                String name = cve.getConstraintName().toLowerCase(Locale.ROOT);
                int dot = name.lastIndexOf('.');
                return dot >= 0 ? name.substring(dot + 1) : name;
            }
            current = current.getCause();
        }
        return null;
    }

    private ResponseEntity<ErrorResponse> buildResponse(HttpStatus status, String message) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                Instant.now(),
                Map.of()
        );
        return ResponseEntity.status(status).body(body);
    }


}

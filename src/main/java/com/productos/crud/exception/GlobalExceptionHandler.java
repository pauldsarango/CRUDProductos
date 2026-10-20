package com.productos.crud.exception;

import com.productos.crud.dto.ErrorResponse;
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
}

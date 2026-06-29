package com.nokhrin.accounting.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.Instant;
import java.util.Map;

public class GlobalExceptionHandler {
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String,Object>>handleValidation(
            MethodArgumentNotValidException exception
    ){
        String message = exception.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getField() + ": " + error)
                .reduce((a,b)->a + "; " + b)
                .orElse("Validation failed: " + exception);
        return ResponseEntity.badRequest().body(Map.of(
                "error", message,
                "timestamp", Instant.now().toString()
        ));
    }

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<Map<String, Object>> handleIllegalArg(
            IllegalArgumentException exception){
        return ResponseEntity.badRequest().body(Map.of(
                "error", exception,
                "timestamp", Instant.now().toString()
        ));
    }
}

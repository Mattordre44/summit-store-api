package com.mattordre.summitstore.exception.handler;

import com.mattordre.summitstore.exception.InvalidArgumentException;
import com.mattordre.summitstore.exception.dto.ErrorResponse;
import com.mattordre.summitstore.image.exception.StorageAccessException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.NoSuchElementException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    @ResponseStatus(HttpStatus.NOT_FOUND)
    @ExceptionHandler(NoSuchElementException.class)
    public ErrorResponse handleNoSuchElementException() {
        return ErrorResponse.builder()
                .message("The requested resource was not found.")
                .errorCode("NOT_FOUND_RESSOURCE")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }


    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(InvalidArgumentException.class)
    public ErrorResponse handleInvalidArgumentExceptions(InvalidArgumentException e) {
        return ErrorResponse.builder()
                .message(e.getMessage())
                .errorCode("INVALID_ARGUMENT")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }


    @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
    @ExceptionHandler(StorageAccessException.class)
    public ErrorResponse handleStorageAccessExceptions(StorageAccessException e) {
        log.error("StorageAccessException occurred: {}", e.getMessage(), e);
        return ErrorResponse.builder()
                .message("An error occurred while accessing the storage. Please try again later.")
                .errorCode("STORAGE_ACCESS_ERROR")
                .timestamp(LocalDateTime.now().toString())
                .build();
    }

}

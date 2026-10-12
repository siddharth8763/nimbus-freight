package com.nimbusfreight.web;

import com.nimbusfreight.engine.InvalidReadingException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class ApiExceptionHandler {

    @ExceptionHandler(InvalidReadingException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public Map<String, String> invalid(InvalidReadingException ex) {
        return Map.of("error", ex.getMessage());
    }

    @ExceptionHandler(TruckNotFoundException.class)
    @ResponseStatus(HttpStatus.NOT_FOUND)
    public Map<String, String> missing(TruckNotFoundException ex) {
        return Map.of("error", ex.getMessage());
    }
}

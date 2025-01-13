package org.example.exception;

import org.example.entity.basic.Data;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.List;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ResponseEntity<Data<?>> handleException(Exception ex) {
        Data<?> response = new Data<>();
        response.setStatus("error");
        response.setMessage(ex.getMessage());
        response.setResults(List.of()); // 设置为空集合
        return new ResponseEntity<>(response, HttpStatus.INTERNAL_SERVER_ERROR);
    }
}

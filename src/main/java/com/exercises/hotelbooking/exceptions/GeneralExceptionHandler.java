package com.exercises.hotelbooking.exceptions;

import com.exercises.hotelbooking.models.ErrorEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.support.DefaultMessageSourceResolvable;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;

import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.springframework.http.HttpStatus.BAD_REQUEST;

@Slf4j
@ControllerAdvice
public class GeneralExceptionHandler extends ResponseEntityExceptionHandler {

    @Override
    protected ResponseEntity<Object> handleMethodArgumentNotValid(MethodArgumentNotValidException ex,
                                                                  HttpHeaders headers, HttpStatus status,
                                                                  WebRequest request) {
        log.error(ex.getMessage());
        setContentType(headers);
        return new ResponseEntity<>(
                new ErrorEntity(BAD_REQUEST.value(), "Invalid input",
                        ex.getBindingResult().getAllErrors().stream()
                                .map(DefaultMessageSourceResolvable::getDefaultMessage)
                                .collect(Collectors.toList())),
                headers, BAD_REQUEST);
    }

    @Override
    protected ResponseEntity<Object> handleExceptionInternal(Exception ex, Object body,
                                                             HttpHeaders headers, HttpStatus status,
                                                             WebRequest request) {
        log.error("Request failed", ex);
        setContentType(headers);
        return super.handleExceptionInternal(ex,
                body == null ? new ErrorEntity(status.value(), ex.getMessage()) : body,
                headers, status, request);
    }

    private void setContentType(HttpHeaders headers) {
        headers.put(HttpHeaders.CONTENT_TYPE, asList("application/json"));
    }
}

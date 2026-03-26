package com.devops.itu_minitwit.Controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class ApiErrorHandler {
  private static final Logger log = LoggerFactory.getLogger(ApiErrorHandler.class);

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  public Map<String, Object> badRequest(IllegalArgumentException ex) {
    log.warn("Bad request: {}", ex.getMessage());
    return Map.of("status", 400, "error_msg", ex.getMessage());
  }
}

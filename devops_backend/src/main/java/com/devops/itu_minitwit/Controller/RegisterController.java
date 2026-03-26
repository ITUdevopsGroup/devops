package com.devops.itu_minitwit.Controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devops.itu_minitwit.dto.RegisterRequest;
import com.devops.itu_minitwit.store.Store;

@RestController
public class RegisterController {
  private static final Logger log = LoggerFactory.getLogger(RegisterController.class);
  private final Store store;

  public RegisterController(Store store) {
    this.store = store;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void register(
      @RequestBody RegisterRequest payload,
      @RequestParam(name = "latest", required = false) Long latest) {

    if (latest != null) {
      store.setLatest(latest);
    }

    log.info("Request: POST /register username={} latest={}",
        payload.getUsername(), latest);

    if (payload.getUsername() == null || payload.getUsername().isBlank()) {
      log.warn("Register failed: missing username");
      throw new IllegalArgumentException("missing username");
    }
    if (payload.getEmail() == null || payload.getEmail().isBlank()) {
      log.warn("Register failed: invalid email for username={}", payload.getUsername());
      throw new IllegalArgumentException("invalid email");
    }
    if (payload.getPwd() == null || payload.getPwd().isBlank()) {
      log.warn("Register failed: missing password for username={}", payload.getUsername());
      throw new IllegalArgumentException("password missing");
    }

    store.registerUser(payload.getUsername(), payload.getEmail(), payload.getPwd());
    log.info("Success: registered username={}", payload.getUsername());
  }
}

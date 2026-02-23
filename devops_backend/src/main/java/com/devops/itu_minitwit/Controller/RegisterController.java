package com.devops.itu_minitwit.Controller;

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

  
  private final Store store;

  public RegisterController(Store store) {
    this.store = store;
  }

  @PostMapping("/register")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void register(
      @RequestBody RegisterRequest payload,
      @RequestParam(name="latest", required=false) Long latest
  ) {
    if (latest != null) store.setLatest(latest);

    // minimal validation for simulator tests
    if (payload.getUsername() == null || payload.getUsername().isBlank()) {
      throw new IllegalArgumentException("missing username");
    }
    if (payload.getEmail() == null || payload.getEmail().isBlank()) {
      throw new IllegalArgumentException("invalid email");
    }
    if (payload.getPwd() == null || payload.getPwd().isBlank()) {
      throw new IllegalArgumentException("password missing");
    }

    store.registerUser(payload.getUsername());
  }
}

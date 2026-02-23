package com.devops.itu_minitwit.Controller;

import java.util.Map;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devops.itu_minitwit.store.Store;

@RestController
public class LatestController {

  private final Store store;

  public LatestController(Store store) {
    this.store = store;
  }

  @GetMapping("/latest")
  public Map<String, Long> latest() {
    return Map.of("latest", store.getLatest());
  }
}

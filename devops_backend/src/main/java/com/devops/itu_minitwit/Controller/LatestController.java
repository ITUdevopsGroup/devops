package com.devops.itu_minitwit.Controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import com.devops.itu_minitwit.store.Store;

@RestController
public class LatestController {
  private static final Logger log = LoggerFactory.getLogger(LatestController.class);
  private final Store store;

  public LatestController(Store store) {
    this.store = store;
  }

  @GetMapping("/latest")
  public Map<String, Long> latest() {
    long latest = store.getLatest();
    log.info("Request: GET /latest latest={}", latest);
    return Map.of("latest", latest);
  }
}

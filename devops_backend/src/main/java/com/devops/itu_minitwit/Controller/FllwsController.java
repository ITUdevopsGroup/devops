package com.devops.itu_minitwit.Controller;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devops.itu_minitwit.dto.FollowAction;
import com.devops.itu_minitwit.store.Store;

@RestController
@RequestMapping("/fllws")
public class FllwsController {
  private static final Logger log = LoggerFactory.getLogger(FllwsController.class);
  private final Store store;

  public FllwsController(Store store) {
    this.store = store;
  }

  @PostMapping("/{username}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void postFllws(
      @PathVariable("username") String username,
      @RequestBody FollowAction payload,
      @RequestParam(name = "latest", required = false) Long latest) {
    boolean hasFollow = payload.getFollow() != null && !payload.getFollow().isBlank();
    boolean hasUnfollow = payload.getUnfollow() != null && !payload.getUnfollow().isBlank();

    log.info("Request: POST /fllws/{} latest={} hasFollow={} hasUnfollow={}",
        username, latest, hasFollow, hasUnfollow);

    if (latest != null) {
      store.setLatest(latest);
    }

    if (hasFollow == hasUnfollow) {
      log.warn("Invalid follow payload for user={} hasFollow={} hasUnfollow={}",
          username, hasFollow, hasUnfollow);
      throw new IllegalArgumentException("Provide exactly one of follow/unfollow");
    }

    if (hasFollow) {
      store.follow(username, payload.getFollow());
      log.info("Success: {} followed {}", username, payload.getFollow());
    } else {
      store.unfollow(username, payload.getUnfollow());
      log.info("Success: {} unfollowed {}", username, payload.getUnfollow());
    }
  }

  @GetMapping("/{username}")
  public Map<String, Object> getFllws(
      @PathVariable("username") String username,
      @RequestParam(name = "no", defaultValue = "100") int no,
      @RequestParam(name = "latest", required = false) Long latest) {
    if (latest != null) {
      store.setLatest(latest);
    }

    log.info("Request: GET /fllws/{} no={} latest={}", username, no, latest);
    return Map.of("follows", store.getFollows(username, no));
  }
}

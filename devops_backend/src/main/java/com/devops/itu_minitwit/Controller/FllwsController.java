package com.devops.itu_minitwit.Controller;

import java.util.Map;

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

  private final Store store;

  public FllwsController(Store store) {
    this.store = store;
  }

  @PostMapping("/{username}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void postFllws(
      @PathVariable("username") String username,
    @RequestBody FollowAction payload,
    @RequestParam(name="latest", required=false) Long latest
  ) {
    System.out.println("payload.follow=" + payload.getFollow() + ", payload.unfollow=" + payload.getUnfollow());

    if (latest != null) store.setLatest(latest);

    boolean hasFollow = payload.getFollow() != null && !payload.getFollow().isBlank();
    boolean hasUnfollow = payload.getUnfollow() != null && !payload.getUnfollow().isBlank();

    if (hasFollow == hasUnfollow) {
      throw new IllegalArgumentException("Provide exactly one of follow/unfollow");
    }

    if (hasFollow) store.follow(username, payload.getFollow());
    else store.unfollow(username, payload.getUnfollow());
  }

  @GetMapping("/{username}")
  public Map<String, Object> getFllws(
       @PathVariable("username") String username,
    @RequestParam(name="no", defaultValue="100") int no,
    @RequestParam(name="latest", required=false) Long latest
  ) {
    if (latest != null) store.setLatest(latest);
    return Map.of("follows", store.getFollows(username, no));
  }
}

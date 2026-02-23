package com.devops.itu_minitwit.Controller;

import java.util.List;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

import com.devops.itu_minitwit.dto.MessageResponse;
import com.devops.itu_minitwit.dto.PostMessage;
import com.devops.itu_minitwit.store.Store;

@RestController
@RequestMapping("/msgs")
public class MsgsController {

  private final Store store;

  public MsgsController(Store store) {
    this.store = store;
  }

  @GetMapping
  public List<MessageResponse> getMsgs(
      @RequestParam(name="no", defaultValue="100") int no,
      @RequestParam(name="latest", required=false) Long latest
  ) {
    if (latest != null) store.setLatest(latest);
    return store.getMessages(no);
  }

  @GetMapping("/{username}")
  public List<MessageResponse> getMsgsByUser(
      @PathVariable("username") String username,
      @RequestParam(name="no", defaultValue="100") int no,
      @RequestParam(name="latest", required=false) Long latest
  ) {
    if (latest != null) store.setLatest(latest);
    // simulator expects 404 if user not found (optional for your tests, but good)
    if (!store.userExists(username)) {
      throw new UserNotFound();
    }
    return store.getMessagesByUser(username, no);
  }

  @PostMapping("/{username}")
  @ResponseStatus(HttpStatus.NO_CONTENT)
  public void postMsg(
      @PathVariable("username") String username,
      @RequestBody PostMessage payload,
      @RequestParam(name="latest", required=false) Long latest
  ) {
    if (latest != null) store.setLatest(latest);
    if (!store.userExists(username)) {
      throw new UserNotFound();
    }
    if (payload.getContent() == null) payload.setContent("");
    store.addMessage(username, payload.getContent());
  }

  @ResponseStatus(HttpStatus.NOT_FOUND)
  private static class UserNotFound extends RuntimeException {}
}

package com.devops.itu_minitwit.store;

import java.util.List;
import com.devops.itu_minitwit.dto.MessageResponse;

public interface Store {
  long getLatest();
  void setLatest(long value);

  void registerUser(String username);
  boolean userExists(String username);

  void follow(String who, String whom);
  void unfollow(String who, String whom);
  List<String> getFollows(String who, int limit);

  void addMessage(String username, String content);
  List<MessageResponse> getMessages(int limit);
  List<MessageResponse> getMessagesByUser(String username, int limit);
}

package com.devops.itu_minitwit.store;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.stereotype.Component;

@Component
public class InMemoryStore implements Store {
  private final AtomicLong latest = new AtomicLong(0);
private final Map<String, Set<String>> follows = new ConcurrentHashMap<>();
private final java.util.Set<String> users = java.util.concurrent.ConcurrentHashMap.newKeySet();
private final java.util.List<com.devops.itu_minitwit.dto.MessageResponse> messages = new CopyOnWriteArrayList();

private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  public long getLatest() {
    return latest.get();
  }

  public void setLatest(long value) {
    latest.set(value);
  }
  public void follow(String who, String whom) {
  follows.computeIfAbsent(who, k -> ConcurrentHashMap.newKeySet()).add(whom);
}

public void unfollow(String who, String whom) {
  Set<String> set = follows.get(who);
  if (set != null) set.remove(whom);
}

public List<String> getFollows(String who, int limit) {
  List<String> list = new ArrayList<>(follows.getOrDefault(who, Collections.emptySet()));
  Collections.sort(list);
  if (limit < list.size()) return list.subList(0, limit);
  return list;
}

public void registerUser(String username) {
  users.add(username);
}

public boolean userExists(String username) {
  return users.contains(username);
}

public void addMessage(String username, String content) {
  String ts = LocalDateTime.now().format(FMT);
  messages.add(0, new com.devops.itu_minitwit.dto.MessageResponse(content, ts, username)); // newest first
}

public java.util.List<com.devops.itu_minitwit.dto.MessageResponse> getMessages(int limit) {
  int n = Math.min(limit, messages.size());
  return new java.util.ArrayList<>(messages.subList(0, n));
}

public java.util.List<com.devops.itu_minitwit.dto.MessageResponse> getMessagesByUser(String username, int limit) {
  java.util.List<com.devops.itu_minitwit.dto.MessageResponse> out = new java.util.ArrayList<>();
  for (var m : messages) {
    if (username.equals(m.getUser())) out.add(m);
    if (out.size() >= limit) break;
  }
  return out;
}


}

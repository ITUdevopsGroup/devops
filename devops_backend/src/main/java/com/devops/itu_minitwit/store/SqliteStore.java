package com.devops.itu_minitwit.store;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import com.devops.itu_minitwit.domain.Follower;
import com.devops.itu_minitwit.domain.FollowerId;
import com.devops.itu_minitwit.domain.Message;
import com.devops.itu_minitwit.domain.Meta;
import com.devops.itu_minitwit.domain.User;
import com.devops.itu_minitwit.dto.MessageResponse;
import com.devops.itu_minitwit.repository.FollowerRepository;
import com.devops.itu_minitwit.repository.MessageRepository;
import com.devops.itu_minitwit.repository.MetaRepository;
import com.devops.itu_minitwit.repository.UserRepository;

@Component
@Primary // IMPORTANT: makes Spring inject this instead of InMemoryStore
public class SqliteStore implements Store {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private final MetaRepository metaRepository;
  private final UserRepository userRepository;
  private final FollowerRepository followerRepository;
  private final MessageRepository messageRepository;

  public SqliteStore(MetaRepository metaRepository,
                     UserRepository userRepository,
                     FollowerRepository followerRepository,
                     MessageRepository messageRepository) {
    this.metaRepository = metaRepository;
    this.userRepository = userRepository;
    this.followerRepository = followerRepository;
    this.messageRepository = messageRepository;
  }

  // --- latest ---
  @Override
  public long getLatest() {
    Optional<Meta> meta = metaRepository.findById("latest");
    if (meta.isEmpty()) {
      return 0L;
    }
    try {
      return Long.parseLong(meta.get().getValue());
    } catch (NumberFormatException e) {
      return 0L;
    }
  }

  @Override
  public void setLatest(long value) {
    Meta meta = metaRepository.findById("latest").orElseGet(Meta::new);
    meta.setKey("latest");
    meta.setValue(Long.toString(value));
    metaRepository.save(meta);
  }

  // --- users ---
  private int ensureUserId(String username) {
    Optional<User> existing = userRepository.findByUsername(username);
    if (existing.isPresent() && existing.get().getId() != null) {
      return existing.get().getId();
    }

    User u = new User();
    u.setUsername(username);
    u.setEmail(username + "@sim.local");
    u.setPwHash("");
    User saved = userRepository.save(u);
    return saved.getId();
  }

  @Override
  public void registerUser(String username) {
    ensureUserId(username);
  }

  @Override
  public boolean userExists(String username) {
    return userRepository.existsByUsername(username);
  }

  // --- follows ---
  @Override
  public void follow(String who, String whom) {
    int whoId = ensureUserId(who);
    int whomId = ensureUserId(whom);

    FollowerId id = new FollowerId(whoId, whomId);
    if (!followerRepository.existsById(id)) {
      Optional<User> whoUserOpt = userRepository.findById(whoId);
      Optional<User> whomUserOpt = userRepository.findById(whomId);
      if (whoUserOpt.isEmpty() || whomUserOpt.isEmpty()) {
        return;
      }
      Follower follower = new Follower();
      follower.setId(id);
      follower.setWho(whoUserOpt.get());
      follower.setWhom(whomUserOpt.get());
      followerRepository.save(follower);
    }
  }

  @Override
  public void unfollow(String who, String whom) {
    Optional<User> whoOpt = userRepository.findByUsername(who);
    Optional<User> whomOpt = userRepository.findByUsername(whom);
    if (whoOpt.isEmpty() || whomOpt.isEmpty()) {
      return;
    }
    FollowerId id = new FollowerId(whoOpt.get().getId(), whomOpt.get().getId());
    if (followerRepository.existsById(id)) {
      followerRepository.deleteById(id);
    }
  }

  @Override
  public List<String> getFollows(String who, int limit) {
    List<Follower> edges = followerRepository.findByWhoUsernameOrderByWhomUsernameAsc(who);
    List<String> out = new ArrayList<>();
    for (Follower f : edges) {
      out.add(f.getWhom().getUsername());
      if (out.size() >= limit) break;
    }
    return out;
  }

  // --- messages ---
  @Override
  public void addMessage(String username, String content) {
    int userId = ensureUserId(username);
    Optional<User> userOpt = userRepository.findById(userId);
    if (userOpt.isEmpty()) {
      return;
    }
    long epoch = Instant.now().getEpochSecond();
    Message m = new Message();
    m.setAuthor(userOpt.get());
    m.setText(content);
    m.setPubDate(epoch);
    m.setFlagged(0);
    messageRepository.save(m);
  }

  private static String fmtEpochSeconds(long seconds) {
    var ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
    return ldt.format(FMT);
  }

  @Override
  public List<MessageResponse> getMessages(int limit) {
    List<Message> all = messageRepository.findByFlaggedOrderByPubDateDesc(0);
    int size = Math.min(limit, all.size());
    List<MessageResponse> out = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Message m = all.get(i);
      String content = m.getText();
      long pub = m.getPubDate() != null ? m.getPubDate() : 0L;
      String user = m.getAuthor().getUsername();
      out.add(new MessageResponse(content, fmtEpochSeconds(pub), user));
    }
    return out;
  }

  @Override
  public List<MessageResponse> getMessagesByUser(String username, int limit) {
    List<Message> all = messageRepository.findByFlaggedAndAuthorUsernameOrderByPubDateDesc(0, username);
    int size = Math.min(limit, all.size());
    List<MessageResponse> out = new ArrayList<>();
    for (int i = 0; i < size; i++) {
      Message m = all.get(i);
      long pub = m.getPubDate() != null ? m.getPubDate() : 0L;
      out.add(new MessageResponse(
        m.getText(),
        fmtEpochSeconds(pub),
        m.getAuthor().getUsername()
      ));
    }
    return out;
  }
}


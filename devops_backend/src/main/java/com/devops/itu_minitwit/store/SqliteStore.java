package com.devops.itu_minitwit.store;

import java.sql.*;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.*;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import com.devops.itu_minitwit.dto.MessageResponse;

@Component
@Primary // IMPORTANT: makes Spring inject this instead of InMemoryStore
public class SqliteStore implements Store {

  private static final DateTimeFormatter FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

  private static final String DEFAULT_DB_PATH = "minitwit.db";

  private static String jdbcUrl() {
    String filePath = System.getenv().getOrDefault("MINITWIT_DB_PATH", DEFAULT_DB_PATH);
    if (filePath.startsWith("jdbc:sqlite:")) return filePath;
    return "jdbc:sqlite:" + filePath;
  }

  public SqliteStore() {
    // ensure meta table exists for persisting "latest"
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var st = conn.createStatement()) {
      st.executeUpdate("""
        CREATE TABLE IF NOT EXISTS meta (
          k TEXT PRIMARY KEY,
          v TEXT NOT NULL
        )
      """);
    } catch (SQLException e) {
      throw new RuntimeException("Failed to init meta table: " + e.getMessage(), e);
    }
  }

  // --- latest ---
  @Override
  public long getLatest() {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("SELECT v FROM meta WHERE k='latest'")) {
      var rs = ps.executeQuery();
      if (rs.next()) return Long.parseLong(rs.getString(1));
      return 0L;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void setLatest(long value) {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("""
           INSERT INTO meta(k,v) VALUES('latest', ?)
           ON CONFLICT(k) DO UPDATE SET v=excluded.v
         """)) {
      ps.setString(1, Long.toString(value));
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // --- users ---
  private int ensureUserId(String username) throws SQLException {
    try (var conn = DriverManager.getConnection(jdbcUrl())) {
      // try fetch
      try (var ps = conn.prepareStatement("SELECT user_id FROM user WHERE username=?")) {
        ps.setString(1, username);
        var rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
      }
      // insert minimal row (simulator doesn't care about email/pw_hash)
      try (var ps = conn.prepareStatement(
          "INSERT INTO user(username, email, pw_hash) VALUES(?, ?, ?)")) {
        ps.setString(1, username);
        ps.setString(2, username + "@sim.local");
        ps.setString(3, ""); // empty hash
        ps.executeUpdate();
      } catch (SQLException insertErr) {
        // if concurrent insert happened, fall through and re-select
      }
      try (var ps = conn.prepareStatement("SELECT user_id FROM user WHERE username=?")) {
        ps.setString(1, username);
        var rs = ps.executeQuery();
        if (rs.next()) return rs.getInt(1);
      }
      throw new SQLException("Could not create/find user_id for " + username);
    }
  }

  @Override
  public void registerUser(String username) {
    try {
      ensureUserId(username);
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public boolean userExists(String username) {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("SELECT 1 FROM user WHERE username=?")) {
      ps.setString(1, username);
      return ps.executeQuery().next();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // --- follows ---
  @Override
  public void follow(String who, String whom) {
    try {
      int whoId = ensureUserId(who);
      int whomId = ensureUserId(whom);
      try (var conn = DriverManager.getConnection(jdbcUrl());
           var ps = conn.prepareStatement(
             "INSERT OR IGNORE INTO follower(who_id, whom_id) VALUES(?, ?)")) {
        ps.setInt(1, whoId);
        ps.setInt(2, whomId);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public void unfollow(String who, String whom) {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("""
           DELETE FROM follower
           WHERE who_id = (SELECT user_id FROM user WHERE username=?)
             AND whom_id = (SELECT user_id FROM user WHERE username=?)
         """)) {
      ps.setString(1, who);
      ps.setString(2, whom);
      ps.executeUpdate();
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<String> getFollows(String who, int limit) {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("""
           SELECT u2.username
           FROM follower f
           JOIN user u1 ON u1.user_id = f.who_id
           JOIN user u2 ON u2.user_id = f.whom_id
           WHERE u1.username = ?
           ORDER BY u2.username ASC
           LIMIT ?
         """)) {
      ps.setString(1, who);
      ps.setInt(2, limit);
      var rs = ps.executeQuery();
      var out = new ArrayList<String>();
      while (rs.next()) out.add(rs.getString(1));
      return out;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  // --- messages ---
  @Override
  public void addMessage(String username, String content) {
    try {
      int userId = ensureUserId(username);
      long epoch = Instant.now().getEpochSecond();
      try (var conn = DriverManager.getConnection(jdbcUrl());
           var ps = conn.prepareStatement(
             "INSERT INTO message(author_id, text, pub_date, flagged) VALUES(?, ?, ?, 0)")) {
        ps.setInt(1, userId);
        ps.setString(2, content);
        ps.setLong(3, epoch);
        ps.executeUpdate();
      }
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  private static String fmtEpochSeconds(long seconds) {
    var ldt = LocalDateTime.ofInstant(Instant.ofEpochSecond(seconds), ZoneId.systemDefault());
    return ldt.format(FMT);
  }

  @Override
  public List<MessageResponse> getMessages(int limit) {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("""
           SELECT m.text, m.pub_date, u.username
           FROM message m
           JOIN user u ON u.user_id = m.author_id
           WHERE m.flagged = 0
           ORDER BY m.pub_date DESC
           LIMIT ?
         """)) {
      ps.setInt(1, limit);
      var rs = ps.executeQuery();
      var out = new ArrayList<MessageResponse>();
      while (rs.next()) {
        String content = rs.getString(1);
        long pub = rs.getLong(2);
        String user = rs.getString(3);
        out.add(new MessageResponse(content, fmtEpochSeconds(pub), user));
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }

  @Override
  public List<MessageResponse> getMessagesByUser(String username, int limit) {
    try (var conn = DriverManager.getConnection(jdbcUrl());
         var ps = conn.prepareStatement("""
           SELECT m.text, m.pub_date, u.username
           FROM message m
           JOIN user u ON u.user_id = m.author_id
           WHERE m.flagged = 0 AND u.username = ?
           ORDER BY m.pub_date DESC
           LIMIT ?
         """)) {
      ps.setString(1, username);
      ps.setInt(2, limit);
      var rs = ps.executeQuery();
      var out = new ArrayList<MessageResponse>();
      while (rs.next()) {
        out.add(new MessageResponse(
          rs.getString(1),
          fmtEpochSeconds(rs.getLong(2)),
          rs.getString(3)
        ));
      }
      return out;
    } catch (SQLException e) {
      throw new RuntimeException(e);
    }
  }
}

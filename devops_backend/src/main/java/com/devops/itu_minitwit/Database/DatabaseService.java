package com.devops.itu_minitwit.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.devops.itu_minitwit.Json.PublicDataContainer;
import com.devops.itu_minitwit.Json.PublicDataRecord;
import com.devops.itu_minitwit.Json.Result;
import com.devops.itu_minitwit.Json.ResultContainer;
import com.devops.itu_minitwit.Json.UserData;
import com.devops.itu_minitwit.Json.UserDataContainer;

public class DatabaseService {

    private final String PUBLIC_SQL = "select message.*, user.* from message, user where message.flagged = 0 and message.author_id = user.user_id order by message.pub_date desc limit 30";
    private final String USER_SQL = "select message.*, user.* from message, user where message.flagged = 0 and message.author_id = user.user_id and (user.user_id = ? or user.user_id in (select whom_id from follower where who_id = ?)) order by message.pub_date desc limit 30";
    private final String REGISTER_SQL = "insert into user (username, email, pw_hash) values (?, ?, ?)";
    private final String SPECIFIC_USER__SQL = "select * from user where username = ?";
    private final String IS_FOLLOWED = "select 1 from follower where follower.who_id = ? and follower.whom_id = ?";
    private final String FOLLOW = "insert into follower (who_id, whom_id) values (?, ?)";
    private final String UNFOLLOW = "delete from follower where who_id=? and whom_id=?";
    private final String ADD_MESSAGE = "insert into message (author_id, text, pub_date, flagged) values (?, ?, ?, 0)";
    private final String GET_USER_ID = "select user_id,username from user where username = ?";
    private final String COUNT_MESSAGES_SQL = "select count(*) as total from message where flagged = 0";
    private final String COUNT_USERS_SQL = "select count(*) as total from user";
    private static final Logger log = LogManager.getLogger();
    private static final String DEFAULT_DB_PATH = "minitwit.db"; // local dev fallback

    // Secondary PostgreSQL connection details (dual-write target)
    private static final String DEFAULT_PG_URL = "jdbc:postgresql://localhost:5432/minitwit";
    private static final String DEFAULT_PG_USER = "postgres";
    private static final String DEFAULT_PG_PASSWORD = "postgres";

    private static String jdbcUrl() {
        String filePath = System.getenv().getOrDefault("MINITWIT_DB_PATH", DEFAULT_DB_PATH);
        // ensure jdbc:sqlite:<file>
        if (filePath.startsWith("jdbc:sqlite:"))
            return filePath;
        return "jdbc:sqlite:" + filePath;
    }

    private static String pgJdbcUrl() {
        return System.getenv().getOrDefault("MINITWIT_PG_URL", DEFAULT_PG_URL);
    }

    private static String pgUser() {
        return System.getenv().getOrDefault("MINITWIT_PG_USER", DEFAULT_PG_USER);
    }

    private static String pgPassword() {
        return System.getenv().getOrDefault("MINITWIT_PG_PASSWORD", DEFAULT_PG_PASSWORD);
    }

    public DatabaseService() {
    }

    public int getTotalMessageCount() {
        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(COUNT_MESSAGES_SQL)) {
            if (rs.next())
                return rs.getInt("total");
        } catch (SQLException e) {
            log.error("Count messages failed: " + e.getMessage());
        }
        return 0;
    }

    public int getTotalUserCount() {
        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(COUNT_USERS_SQL)) {
            if (rs.next())
                return rs.getInt("total");
        } catch (SQLException e) {
            log.error("Count users failed: " + e.getMessage());
        }
        return 0;
    }

    public static Connection connect() {

        try (
                Connection conn = DriverManager.getConnection(jdbcUrl())) {
            log.info("Connection to SQLite has been established.");
            return conn;
        } catch (SQLException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    public PublicDataContainer getPublicData() {
        log.info("Querying public data records");
        try (
                Connection conn = DriverManager.getConnection(jdbcUrl());
                var stmt = conn.createStatement();
                var rs = stmt.executeQuery(PUBLIC_SQL)) {
            ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();
            while (rs.next()) {
                PublicDataRecord record = new PublicDataRecord(rs.getInt("message_id"), rs.getString("author_id"),
                        rs.getString("text"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new Date(rs.getInt("pub_date") * 1000L)),
                        rs.getInt("flagged"),
                        rs.getInt("user_id"), rs.getString("username"), rs.getString("email"), rs.getString("pw_hash"));
                data.add(record);
            }
            PublicDataContainer result = new PublicDataContainer(data, false);
            conn.close();
            log.info(String.format("Querying public data of succeded"));
            return result;
        } catch (SQLException e) {
            log.error(String.format("Querying public data of failed" + " " + e.getMessage()));
            System.err.println(e.getMessage());
        }
        return null;
    }

    public PublicDataContainer getUserData(int sessionUser, String profileUser) {

        log.info("Querying user data records for user: " + sessionUser);
        ResultContainer followed = isFollowed(sessionUser, profileUser != null ? profileUser : "");

        UserDataContainer userdata = getUserId(profileUser);

        if (userdata != null && userdata.getUserData().getUserId() == 0) {
            log.error("User doesnt exist: " + profileUser);
            return new PublicDataContainer();
        } else if (userdata == null)
            return new PublicDataContainer();

        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(USER_SQL)) {
            pstmt.setInt(1, userdata.getUserData().getUserId());
            pstmt.setInt(2, userdata.getUserData().getUserId());

            var rs = pstmt.executeQuery();
            ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();

            while (rs.next()) {
                PublicDataRecord record = new PublicDataRecord(rs.getInt("message_id"), rs.getString("author_id"),
                        rs.getString("text"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                                .format(new Date(rs.getInt("pub_date") * 1000L)),
                        rs.getInt("flagged"),
                        rs.getInt("user_id"), rs.getString("username"), rs.getString("email"), rs.getString("pw_hash"));
                data.add(record);
            }
            PublicDataContainer result = new PublicDataContainer(data, followed.getUserData().isResult());
            conn.close();
            log.info(String.format("Querying user data of  user: {} succeded", sessionUser));
            return result;
        } catch (SQLException e) {
            log.error(String.format("Querying user data of  user: {} failed" + " " + e.getMessage(), sessionUser));
            System.err.println(e.getMessage());
        }
        return null;
    }

    public UserDataContainer getSpecificUserData(String userId, String pwdHash) {

        log.info("Querying user data records for user: " + userId);
        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(SPECIFIC_USER__SQL)) {
            pstmt.setString(1, userId);

            var rs = pstmt.executeQuery();
            UserData userData = new UserData();

            while (rs.next()) {
                userData.setUsername(rs.getString("username"));
                userData.setUserId(rs.getInt("user_id"));
                if (pwdHash.equals(rs.getString("pw_hash"))) {
                    userData.setPwOK(true);
                }
            }
            UserDataContainer data = new UserDataContainer(userData);
            conn.close();
            return data;
        } catch (SQLException e) {
            log.error(String.format("Querying specific user data of  user: {} failed" + " " + e.getMessage(), userId));
            System.err.println(e.getMessage());
        }
        return null;
    }

    public UserDataContainer getUserId(String username) {

        log.info("Get use id: " + username);
        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(GET_USER_ID)) {
            pstmt.setString(1, username);

            var rs = pstmt.executeQuery();
            UserData userData = new UserData();

            while (rs.next()) {
                userData.setUserId(rs.getInt("user_id"));
            }
            UserDataContainer data = new UserDataContainer(userData);
            pstmt.close();
            conn.close();
            return data;
        } catch (SQLException e) {
            log.error(String.format("Get use id: {} failed" + " " + e.getMessage(), username));
            System.err.println(e.getMessage());
        }
        return null;
    }

    private String getUsernameById(int userId) {
        final String sql = "select username from user where user_id = ?";
        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(sql)) {
            pstmt.setInt(1, userId);
            var rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getString("username");
            }
        } catch (SQLException e) {
            log.error(String.format("Get username by id: %d failed %s", userId, e.getMessage()));
        }
        return null;
    }

    public ResultContainer registerNewUser(String username, String email, String pwdHash) {
        log.info("Registring new user: " + username);
        UserDataContainer userdata = getUserId(username);
        if (userdata != null && userdata.getUserData().getUserId() != 0) {
            log.error("User already exists: " + username);
            return new ResultContainer(new Result("The username is already taken", true, false));
        }

        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(REGISTER_SQL)) {
            conn.setAutoCommit(false);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, pwdHash);
            pstmt.executeUpdate();
            log.info(String.format("Succesfully registred new user: {}, email: {}", username, email));
            pstmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            log.error(String.format("Registration of new user: {}, email: {} failed." + " " + e.getMessage(), username,
                    email));
            return new ResultContainer(new Result(String.format("DB_ERROR"), true, false));
        }

        // Best-effort mirror to PostgreSQL
        try (
                var conn = DriverManager.getConnection(pgJdbcUrl(), pgUser(), pgPassword());
                var pstmt = conn.prepareStatement(
                        "insert into \"user\" (username, email, pw_hash) values (?, ?, ?) on conflict (username) do nothing")) {
            conn.setAutoCommit(false);
            pstmt.setString(1, username);
            pstmt.setString(2, email);
            pstmt.setString(3, pwdHash);
            pstmt.executeUpdate();
            pstmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            log.error(String.format("PostgreSQL dual-write failed for new user: %s, email: %s. %s", username, email,
                    e.getMessage()));
        }
        return new ResultContainer(new Result("OK", false, true));

    }

    public ResultContainer isFollowed(int sessionUser, String profileUser) {
        log.info("Checkking follow status for user: " + profileUser);
        UserDataContainer profileUserId = getUserId(profileUser);

        boolean result = false;
        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(IS_FOLLOWED)) {
            conn.setAutoCommit(false);
            pstmt.setInt(1, sessionUser);
            pstmt.setInt(2, profileUserId.getUserData().getUserId());
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                result = true;
            }
            log.info(String.format("Succesfully checked follow status: {}, profileUser: {}", sessionUser, profileUser));
            pstmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            log.error(String.format("Check of follow status: {}, profileUser: {} failed" + " " + e.getMessage(),
                    sessionUser, profileUser));
            return new ResultContainer(new Result(String.format("DB_ERROR"), true, false));

        }
        return new ResultContainer(new Result("OK", false, result));
    }

    public ResultContainer follow(String userId, String whoUsername) {
        log.info("Follow user: " + userId);
        int whom;
        UserDataContainer userdata = getUserId(whoUsername);
        if (userdata != null && userdata.getUserData().getUserId() != 0)
            whom = userdata.getUserData().getUserId();
        else {
            log.error("User doesnt exists: " + whoUsername);
            return new ResultContainer(new Result(String.format("NOT_EXISTS"), true, false));
        }

        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(FOLLOW)) {
            conn.setAutoCommit(false);

            pstmt.setString(1, userId);
            pstmt.setInt(2, whom);

            pstmt.executeUpdate();
            log.info(String.format("Succesfully followed: {}, profileUser: {}", userId, whom));
            pstmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            log.error(String.format("Follow {}, profileUser: {} failed " + " " + e.getMessage(), userId, whom));
            return new ResultContainer(new Result(String.format("DB_ERROR"), true, false));
        }

        // Best-effort mirror to PostgreSQL
        try {
            int whoId = Integer.parseInt(userId);
            String followerUsername = getUsernameById(whoId);
            if (followerUsername != null) {
                // Ensure both users exist in PostgreSQL and insert follower edge
                try (var conn = DriverManager.getConnection(pgJdbcUrl(), pgUser(), pgPassword())) {
                    conn.setAutoCommit(false);
                    try (var ensureUser = conn.prepareStatement(
                            "insert into \"user\"(username, email, pw_hash) values(?, ?, ?) on conflict (username) do nothing")) {
                        ensureUser.setString(1, followerUsername);
                        ensureUser.setString(2, followerUsername + "@sim.local");
                        ensureUser.setString(3, "");
                        ensureUser.executeUpdate();
                        ensureUser.setString(1, whoUsername);
                        ensureUser.setString(2, whoUsername + "@sim.local");
                        ensureUser.setString(3, "");
                        ensureUser.executeUpdate();
                    }
                    try (var ps = conn.prepareStatement(
                            """
                                    insert into follower(who_id, whom_id)
                                    select u1.user_id, u2.user_id
                                    from "user" u1, "user" u2
                                    where u1.username = ? and u2.username = ?
                                    on conflict do nothing
                                    """)) {
                        ps.setString(1, followerUsername);
                        ps.setString(2, whoUsername);
                        ps.executeUpdate();
                    }
                    conn.commit();
                }
            }
        } catch (Exception e) {
            log.error(String.format("PostgreSQL dual-write failed for follow %s, profileUser: %s. %s", userId,
                    whoUsername, e.getMessage()));
        }
        return new ResultContainer(new Result("OK", false, true));
    }

    public ResultContainer unFollow(String userId, String whoUsername) {
        log.info("Unfollow user: " + userId);
        int whom;
        UserDataContainer userdata = getUserId(whoUsername);
        if (userdata != null && userdata.getUserData().getUserId() != 0)
            whom = userdata.getUserData().getUserId();
        else {
            log.error("User doesnt exist: " + whoUsername);
            return new ResultContainer(new Result(String.format("NOT_EXISTS"), true, false));
        }

        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(UNFOLLOW)) {
            conn.setAutoCommit(false);

            pstmt.setString(1, userId);
            pstmt.setInt(2, whom);

            pstmt.executeUpdate();
            log.info(String.format("Succesfully unfollowed: {}, profileUser: {}", userId, whom));
            pstmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            log.error(String.format("Follow {}, unprofileUser: {} failed" + " " + e.getMessage(), userId, whom));
            return new ResultContainer(new Result(String.format("NOT_EXISTS"), true, false));
        }

        // Best-effort mirror to PostgreSQL
        try {
            int whoId = Integer.parseInt(userId);
            String followerUsername = getUsernameById(whoId);
            if (followerUsername != null) {
                try (var conn = DriverManager.getConnection(pgJdbcUrl(), pgUser(), pgPassword());
                     var ps = conn.prepareStatement(
                             """
                                     delete from follower
                                     where who_id = (select user_id from "user" where username=?)
                                       and whom_id = (select user_id from "user" where username=?)
                                     """)) {
                    conn.setAutoCommit(false);
                    ps.setString(1, followerUsername);
                    ps.setString(2, whoUsername);
                    ps.executeUpdate();
                    conn.commit();
                }
            }
        } catch (Exception e) {
            log.error(String.format("PostgreSQL dual-write failed for unfollow %s, profileUser: %s. %s", userId,
                    whoUsername, e.getMessage()));
        }
        return new ResultContainer(new Result("OK", false, true));
    }

    public ResultContainer addMessage(String userId, String text, String pubDate, String flagged) {
        log.info("Add message for user: " + userId);

        try (
                var conn = DriverManager.getConnection(jdbcUrl());
                var pstmt = conn.prepareStatement(ADD_MESSAGE)) {
            conn.setAutoCommit(false);
            pstmt.setString(1, userId);
            pstmt.setString(2, text);
            pstmt.setString(3, pubDate);

            pstmt.executeUpdate();
            log.info(String.format("Succesfully added message for: {}", userId));
            pstmt.close();
            conn.commit();
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            log.error(String.format("Add message failed for: {}" + e.getMessage(), userId));
            return new ResultContainer(new Result(String.format("NOT_EXISTS"), true, false));
        }

        // Best-effort mirror to PostgreSQL
        try {
            int uid = Integer.parseInt(userId);
            String username = getUsernameById(uid);
            if (username != null) {
                try (var conn = DriverManager.getConnection(pgJdbcUrl(), pgUser(), pgPassword())) {
                    conn.setAutoCommit(false);
                    // ensure user exists
                    try (var ensureUser = conn.prepareStatement(
                            "insert into \"user\"(username, email, pw_hash) values(?, ?, ?) on conflict (username) do nothing")) {
                        ensureUser.setString(1, username);
                        ensureUser.setString(2, username + "@sim.local");
                        ensureUser.setString(3, "");
                        ensureUser.executeUpdate();
                    }
                    try (var ps = conn.prepareStatement(
                            """
                                    insert into message(author_id, text, pub_date, flagged)
                                    select u.user_id, ?, ?, 0
                                    from "user" u
                                    where u.username = ?
                                    """)) {
                        ps.setString(1, text);
                        ps.setString(2, pubDate);
                        ps.setString(3, username);
                        ps.executeUpdate();
                    }
                    conn.commit();
                }
            }
        } catch (Exception e) {
            log.error(String.format("PostgreSQL dual-write failed for add message for: %s %s", userId, e.getMessage()));
        }
        return new ResultContainer(new Result("OK", false, true));
    }
}

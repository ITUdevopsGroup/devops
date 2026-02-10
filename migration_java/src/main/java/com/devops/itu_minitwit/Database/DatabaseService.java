package com.devops.itu_minitwit.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.devops.itu_minitwit.Json.PublicDataContainer;
import com.devops.itu_minitwit.Json.PublicDataRecord;
import com.devops.itu_minitwit.Json.UserData;
import com.devops.itu_minitwit.Json.UserDataContainer;

public class DatabaseService {

    private final String PUBLIC_SQL = "select message.*, user.* from message, user where message.flagged = 0 and message.author_id = user.user_id order by message.pub_date desc limit 30";
    private final String USER_SQL = "select message.*, user.* from message, user where message.flagged = 0 and message.author_id = user.user_id and (user.user_id = ? or user.user_id in (select whom_id from follower where who_id = ?)) order by message.pub_date desc limit 30";
    private final String REGISTER_SQL = "insert into user (username, email, pw_hash) values (?, ?, ?)";
    private final String SPECIFIC_USER__SQL = "select * from user where username = ?";

    private static final Logger log = LogManager.getLogger();
    private static final String PATH = "jdbc:sqlite:minitwit.db";

    public DatabaseService() {
    }

    public static Connection connect() {

        try (
            Connection conn = DriverManager.getConnection(PATH)) {
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
            Connection conn = DriverManager.getConnection(PATH);
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(PUBLIC_SQL)) {
            ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();
            while (rs.next()) {
                PublicDataRecord record = new PublicDataRecord(rs.getInt("message_id"), rs.getString("author_id"),
                        rs.getString("text"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                          .format(new Date(rs.getInt("pub_date") * 1000L)), rs.getInt("flagged"),
                    rs.getInt("user_id"),rs.getString("username"),rs.getString("email"),rs.getString("pw_hash"));
                data.add(record);
            }
            PublicDataContainer result = new PublicDataContainer(data);
            conn.close();
            log.info(String.format("Querying public data of succeded"));
            return result;
        } catch (SQLException e) {
            log.error(String.format("Querying public data of failed"));
            System.err.println(e.getMessage());
        }
        return null;
    }

    public PublicDataContainer getUserData(int userId) {

    log.info("Querying user data records for user: " + userId);
        try (
            var conn = DriverManager.getConnection(PATH);
            var pstmt = conn.prepareStatement(USER_SQL)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, userId);

            var rs = pstmt.executeQuery();
            ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();

            while (rs.next()) {
                PublicDataRecord record = new PublicDataRecord(rs.getInt("message_id"), rs.getString("author_id"),
                        rs.getString("text"), new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                          .format(new Date(rs.getInt("pub_date") * 1000L)), rs.getInt("flagged"),
                    rs.getInt("user_id"),rs.getString("username"),rs.getString("email"),rs.getString("pw_hash"));
                data.add(record);
            }
            PublicDataContainer result = new PublicDataContainer(data);
            conn.close();
            log.info(String.format("Querying user data of  user: {} succeded", userId));
            return result;
        } catch (SQLException e) {
            log.error(String.format("Querying user data of  user: {} failed", userId));
            System.err.println(e.getMessage());
        }
        return null;
    }

    public UserDataContainer getSpecificUserData(String userId, String pwdHash) {

    log.info("Querying user data records for user: " + userId);
        try (
            var conn = DriverManager.getConnection(PATH);
            var pstmt = conn.prepareStatement(SPECIFIC_USER__SQL)) {
            pstmt.setString(1, userId);

            var rs = pstmt.executeQuery();
            UserData userData = new UserData();

            while (rs.next()) {
                userData.setUsername(rs.getString("username"));
                userData.setUserId(rs.getInt("user_id"));
                if(pwdHash.equals(rs.getString("pw_hash"))) {
                    userData.setPwOK(true);
                }
            }
            UserDataContainer data = new UserDataContainer(userData);
            return data;
        } catch (SQLException e) {
            log.error(String.format("Querying specific user data of  user: {} failed", userId));
            System.err.println(e.getMessage());
        }
        return null;
    }

    

    public boolean registerNewUser(String userId,String email,String pwdHash) {
    log.info("Registring new user: " + userId);

        try (
            var conn = DriverManager.getConnection(PATH);
            var pstmt = conn.prepareStatement(REGISTER_SQL)) {
                conn.setAutoCommit(false);
                pstmt.setString(1, userId);
                pstmt.setString(2, email);
                pstmt.setString(3, pwdHash);
                pstmt.executeUpdate();
                log.info(String.format("Succesfully registred new user: {}, email: {}", userId,email));
                pstmt.close();
                conn.commit();
                conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
            log.error(String.format("Registration of new user: {}, email: {} failed.", userId,email));
            return false;
        }
        return true;
    }
}

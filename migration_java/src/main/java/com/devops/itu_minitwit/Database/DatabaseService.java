package com.devops.itu_minitwit.Database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.devops.itu_minitwit.Json.PublicDataContainer;
import com.devops.itu_minitwit.Json.PublicDataRecord;

public class DatabaseService {
    private final String PUBLIC_SQL = "select message.*, user.* from message, user where message.flagged = 0 and message.author_id = user.user_id order by message.pub_date desc limit 30";
    private static final Logger log = LogManager.getLogger();
    private static final String databasePath = "jdbc:sqlite:minitwit.db";

    public DatabaseService() {
    }

    public static Connection connect() {

        try (
            Connection conn = DriverManager.getConnection(databasePath)) {
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
            Connection conn = DriverManager.getConnection(databasePath);
            var stmt = conn.createStatement();
            var rs = stmt.executeQuery(PUBLIC_SQL)) {
            ArrayList<PublicDataRecord> data = new ArrayList<PublicDataRecord>();
            while (rs.next()) {
                PublicDataRecord record = new PublicDataRecord(rs.getInt("message_id"), rs.getString("author_id"),
                        rs.getString("text"), rs.getInt("pub_date"), rs.getInt("flagged"));
                data.add(record);
            }
            PublicDataContainer result = new PublicDataContainer(data);
            conn.close();
            return result;
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        return null;
    }
}

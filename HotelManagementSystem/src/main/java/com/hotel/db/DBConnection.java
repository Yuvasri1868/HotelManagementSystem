package com.hotel.db;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Central place for the MySQL connection settings.
 * CHANGE DB_PASSWORD to your own MySQL root password.
 */
public class DBConnection {
    private static final String HOST = "localhost";
    private static final String PORT = "3306";
    public static final String DB_NAME = "hotel_management";
    private static final String DB_USER = "root";
    private static final String DB_PASSWORD = "";   // <-- CHANGE THIS

    private static final String OPTIONS =
            "?useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC";

    /** Connection to MySQL server (without selecting a database). */
    public static Connection getServerConnection() throws SQLException {
        return DriverManager.getConnection("jdbc:mysql://" + HOST + ":" + PORT + "/" + OPTIONS,
                DB_USER, DB_PASSWORD);
    }

    /** Connection to the hotel_management database. */
    public static Connection getConnection() throws SQLException {
        return DriverManager.getConnection(
                "jdbc:mysql://" + HOST + ":" + PORT + "/" + DB_NAME + OPTIONS, DB_USER, DB_PASSWORD);
    }
}

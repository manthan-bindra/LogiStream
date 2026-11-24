package com.logistream.database;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

/** Opens MySQL connections through the standard JDBC DriverManager API. */
public class DatabaseConnection {
    public static final String URL = setting("LOGISTREAM_DB_URL", "jdbc:mysql://localhost:3306/logistream");
    public static final String USER = setting("LOGISTREAM_DB_USER", "root");
    public static final String PASSWORD = setting("LOGISTREAM_DB_PASSWORD", "");
    public static final String STAFF_USERNAME = setting("LOGISTREAM_STAFF_USERNAME", "staff");
    public static final String STAFF_PASSWORD = setting("LOGISTREAM_STAFF_PASSWORD", "staff123");

    public Connection open() throws SQLException {
        return DriverManager.getConnection(URL, USER, PASSWORD);
    }

    private static String setting(String name, String fallback) {
        String value = System.getenv(name);
        return value == null ? fallback : value;
    }
}

package com.cst8411.finalproject.db;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

public class Database {

    private final String databasePath;

    public Database(String databasePath) {
        this.databasePath = databasePath;
    }

    public Connection getConnection() {
        try {
            Connection connection = DriverManager.getConnection("jdbc:sqlite:" + databasePath);
            try (Statement statement = connection.createStatement()) {
                statement.execute("PRAGMA foreign_keys = ON");
            }
            return connection;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to open database connection.", exception);
        }
    }

    public void initialize() {
        try {
            Path parent = Path.of(databasePath).getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }

            try (Connection connection = getConnection();
                 Statement statement = connection.createStatement()) {
                statement.executeUpdate(loadSchema());
            }
        } catch (IOException | SQLException exception) {
            throw new RuntimeException("Unable to initialize database schema.", exception);
        }
    }

    private String loadSchema() throws IOException {
        try (InputStream inputStream = Database.class.getResourceAsStream("/schema.sql")) {
            if (inputStream == null) {
                throw new IOException("schema.sql resource not found.");
            }
            return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
        }
    }
}

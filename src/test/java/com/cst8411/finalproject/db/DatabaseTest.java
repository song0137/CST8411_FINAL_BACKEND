package com.cst8411.finalproject.db;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.nio.file.Files;
import java.nio.file.Path;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;

class DatabaseTest {

    @Test
    void shouldCreateAllRequiredTables() throws Exception {
        Path dbFile = Files.createTempFile("inventory-schema", ".db");
        Database database = new Database(dbFile.toString());

        database.initialize();

        try (Connection connection = database.getConnection()) {
            Set<String> tables = new HashSet<>();
            try (ResultSet resultSet = connection.createStatement().executeQuery(
                    "SELECT name FROM sqlite_master WHERE type = 'table'")) {
                while (resultSet.next()) {
                    tables.add(resultSet.getString("name"));
                }
            }

            assertTrue(tables.contains("customers"));
            assertTrue(tables.contains("vehicles"));
            assertTrue(tables.contains("devices"));
            assertTrue(tables.contains("vehicle_devices"));
            assertTrue(tables.contains("device_orders"));
            assertTrue(tables.contains("customer_accounts"));
            assertTrue(tables.contains("customer_sessions"));
            assertTrue(tables.contains("maintenance_records"));
        } catch (SQLException exception) {
            throw new RuntimeException(exception);
        }
    }
}

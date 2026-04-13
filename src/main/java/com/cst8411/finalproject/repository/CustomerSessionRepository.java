package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.CustomerSession;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;

public class CustomerSessionRepository {

    private final Database database;

    public CustomerSessionRepository(Database database) {
        this.database = database;
    }

    public CustomerSession create(String token, long customerAccountId, String expiresAt) {
        String sql = "INSERT INTO customer_sessions(token, customer_account_id, expires_at) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            statement.setLong(2, customerAccountId);
            statement.setString(3, expiresAt);
            statement.executeUpdate();
            return findByToken(token);
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create customer session.", exception);
        }
    }

    public CustomerSession findByToken(String token) {
        String sql = "SELECT * FROM customer_sessions WHERE token = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, token);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch customer session.", exception);
        }
    }

    private CustomerSession map(ResultSet resultSet) throws SQLException {
        return new CustomerSession(
                resultSet.getString("token"),
                resultSet.getLong("customer_account_id"),
                resultSet.getString("expires_at"),
                resultSet.getString("created_at")
        );
    }
}

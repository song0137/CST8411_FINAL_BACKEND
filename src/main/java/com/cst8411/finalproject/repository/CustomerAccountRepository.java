package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.CustomerAccount;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CustomerAccountRepository {

    private final Database database;

    public CustomerAccountRepository(Database database) {
        this.database = database;
    }

    public CustomerAccount create(long customerId, String username, String passwordHash) {
        String sql = "INSERT INTO customer_accounts(customer_id, username, password_hash) VALUES (?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setString(2, username);
            statement.setString(3, passwordHash);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for customer account.");
                }
                return findById(keys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create customer account.", exception);
        }
    }

    public CustomerAccount findById(long id) {
        String sql = "SELECT * FROM customer_accounts WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch customer account.", exception);
        }
    }

    public CustomerAccount findByUsername(String username) {
        String sql = "SELECT * FROM customer_accounts WHERE username = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch customer account by username.", exception);
        }
    }

    public CustomerAccount findByCustomerId(long customerId) {
        String sql = "SELECT * FROM customer_accounts WHERE customer_id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch customer account by customer.", exception);
        }
    }

    private CustomerAccount map(ResultSet resultSet) throws SQLException {
        return new CustomerAccount(
                resultSet.getLong("id"),
                resultSet.getLong("customer_id"),
                resultSet.getString("username"),
                resultSet.getString("password_hash"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at")
        );
    }
}

package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Customer;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class CustomerRepository {

    private final Database database;

    public CustomerRepository(Database database) {
        this.database = database;
    }

    public Customer create(String fullName, String phone, String email) {
        String sql = "INSERT INTO customers(full_name, phone, email) VALUES (?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, fullName);
            statement.setString(2, phone);
            statement.setString(3, email);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for customer.");
                }
                return findById(keys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create customer.", exception);
        }
    }

    public Customer findById(long id) {
        String sql = "SELECT * FROM customers WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch customer.", exception);
        }
    }

    public List<Customer> findByName(String nameQuery) {
        String sql = "SELECT * FROM customers WHERE lower(full_name) LIKE lower(?) ORDER BY full_name";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, "%" + nameQuery + "%");
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    customers.add(map(resultSet));
                }
            }
            return customers;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to search customers.", exception);
        }
    }

    public List<Customer> listAll() {
        String sql = "SELECT * FROM customers ORDER BY created_at DESC";
        List<Customer> customers = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                customers.add(map(resultSet));
            }
            return customers;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list customers.", exception);
        }
    }

    private Customer map(ResultSet resultSet) throws SQLException {
        return new Customer(
                resultSet.getLong("id"),
                resultSet.getString("full_name"),
                resultSet.getString("phone"),
                resultSet.getString("email"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at")
        );
    }
}

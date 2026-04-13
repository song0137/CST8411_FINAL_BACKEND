package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Vehicle;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class VehicleRepository {

    private final Database database;

    public VehicleRepository(Database database) {
        this.database = database;
    }

    public Vehicle create(long customerId, String vin, String make, String model, int year, String licensePlate) {
        String sql = "INSERT INTO vehicles(customer_id, vin, make, model, year, license_plate) VALUES (?, ?, ?, ?, ?, ?)";

        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setString(2, vin);
            statement.setString(3, make);
            statement.setString(4, model);
            statement.setInt(5, year);
            statement.setString(6, licensePlate);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for vehicle.");
                }
                return findById(keys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create vehicle.", exception);
        }
    }

    public Vehicle findById(long id) {
        String sql = "SELECT * FROM vehicles WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch vehicle.", exception);
        }
    }

    public Vehicle findByVin(String vin) {
        String sql = "SELECT * FROM vehicles WHERE vin = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, vin);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch vehicle by VIN.", exception);
        }
    }

    public List<Vehicle> listByCustomerId(long customerId) {
        String sql = "SELECT * FROM vehicles WHERE customer_id = ? ORDER BY created_at DESC";
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    vehicles.add(map(resultSet));
                }
            }
            return vehicles;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list vehicles for customer.", exception);
        }
    }

    public List<Vehicle> listAll() {
        String sql = "SELECT * FROM vehicles ORDER BY created_at DESC";
        List<Vehicle> vehicles = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                vehicles.add(map(resultSet));
            }
            return vehicles;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list vehicles.", exception);
        }
    }

    private Vehicle map(ResultSet resultSet) throws SQLException {
        return new Vehicle(
                resultSet.getLong("id"),
                resultSet.getLong("customer_id"),
                resultSet.getString("vin"),
                resultSet.getString("make"),
                resultSet.getString("model"),
                resultSet.getInt("year"),
                resultSet.getString("license_plate"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at")
        );
    }
}

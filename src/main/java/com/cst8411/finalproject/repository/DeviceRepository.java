package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.Device;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeviceRepository {

    private final Database database;

    public DeviceRepository(Database database) {
        this.database = database;
    }

    public Device create(String deviceName, String sku, String serialNumber, String status) {
        String sql = "INSERT INTO devices(device_name, sku, serial_number, status) VALUES (?, ?, ?, ?)";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setString(1, deviceName);
            statement.setString(2, sku);
            statement.setString(3, serialNumber);
            statement.setString(4, status);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for device.");
                }
                return findById(keys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create device.", exception);
        }
    }

    public Device findById(long id) {
        String sql = "SELECT * FROM devices WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch device.", exception);
        }
    }

    public void updateStatus(long id, String status) {
        String sql = "UPDATE devices SET status = ?, updated_at = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, status);
            statement.setLong(2, id);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to update device status.", exception);
        }
    }

    public List<Device> listAll() {
        String sql = "SELECT * FROM devices ORDER BY created_at DESC";
        List<Device> devices = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                devices.add(map(resultSet));
            }
            return devices;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list devices.", exception);
        }
    }

    public List<Device> listByVehicleId(long vehicleId) {
        String sql = """
                SELECT d.*
                FROM devices d
                JOIN vehicle_devices vd ON vd.device_id = d.id
                WHERE vd.vehicle_id = ? AND vd.removed_at IS NULL
                ORDER BY vd.installed_at DESC
                """;
        List<Device> devices = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, vehicleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    devices.add(map(resultSet));
                }
            }
            return devices;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list devices by vehicle.", exception);
        }
    }

    private Device map(ResultSet resultSet) throws SQLException {
        return new Device(
                resultSet.getLong("id"),
                resultSet.getString("device_name"),
                resultSet.getString("sku"),
                resultSet.getString("serial_number"),
                resultSet.getString("status"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at")
        );
    }
}

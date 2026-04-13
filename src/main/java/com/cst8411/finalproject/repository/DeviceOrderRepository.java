package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.DeviceOrder;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class DeviceOrderRepository {

    private final Database database;

    public DeviceOrderRepository(Database database) {
        this.database = database;
    }

    public long create(long customerId, Long vehicleId, String deviceName, String sku, double amountPaid, String status) {
        String sql = """
                INSERT INTO device_orders(customer_id, vehicle_id, device_name, sku, amount_paid, status)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            if (vehicleId == null) {
                statement.setNull(2, java.sql.Types.BIGINT);
            } else {
                statement.setLong(2, vehicleId);
            }
            statement.setString(3, deviceName);
            statement.setString(4, sku);
            statement.setDouble(5, amountPaid);
            statement.setString(6, status);
            statement.executeUpdate();

            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for order.");
                }
                return keys.getLong(1);
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create device order.", exception);
        }
    }

    public List<DeviceOrder> listAll() {
        String sql = "SELECT * FROM device_orders ORDER BY created_at DESC";
        List<DeviceOrder> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            while (resultSet.next()) {
                orders.add(map(resultSet));
            }
            return orders;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list orders.", exception);
        }
    }

    public List<DeviceOrder> listByVehicleId(long vehicleId) {
        String sql = "SELECT * FROM device_orders WHERE vehicle_id = ? ORDER BY created_at DESC";
        List<DeviceOrder> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, vehicleId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(map(resultSet));
                }
            }
            return orders;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list orders by vehicle.", exception);
        }
    }

    public List<DeviceOrder> listByCustomerId(long customerId) {
        String sql = "SELECT * FROM device_orders WHERE customer_id = ? ORDER BY created_at DESC";
        List<DeviceOrder> orders = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, customerId);
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    orders.add(map(resultSet));
                }
            }
            return orders;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list orders by customer.", exception);
        }
    }

    private DeviceOrder map(ResultSet resultSet) throws SQLException {
        long vehicleId = resultSet.getLong("vehicle_id");
        Long maybeVehicleId = resultSet.wasNull() ? null : vehicleId;
        return new DeviceOrder(
                resultSet.getLong("id"),
                resultSet.getLong("customer_id"),
                maybeVehicleId,
                resultSet.getString("device_name"),
                resultSet.getString("sku"),
                resultSet.getDouble("amount_paid"),
                resultSet.getString("status"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at")
        );
    }
}

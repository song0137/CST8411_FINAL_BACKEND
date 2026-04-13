package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.VehicleDeviceBinding;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class VehicleDeviceRepository {

    private final Database database;

    public VehicleDeviceRepository(Database database) {
        this.database = database;
    }

    public VehicleDeviceBinding bind(long deviceId, long vehicleId) {
        String closeExistingSql = "UPDATE vehicle_devices SET removed_at = CURRENT_TIMESTAMP WHERE device_id = ? AND removed_at IS NULL";
        String insertSql = "INSERT INTO vehicle_devices(vehicle_id, device_id) VALUES (?, ?)";

        try (Connection connection = database.getConnection()) {
            connection.setAutoCommit(false);

            try (PreparedStatement closeStatement = connection.prepareStatement(closeExistingSql)) {
                closeStatement.setLong(1, deviceId);
                closeStatement.executeUpdate();
            }

            long bindingId;
            try (PreparedStatement insertStatement = connection.prepareStatement(insertSql, Statement.RETURN_GENERATED_KEYS)) {
                insertStatement.setLong(1, vehicleId);
                insertStatement.setLong(2, deviceId);
                insertStatement.executeUpdate();
                try (ResultSet keys = insertStatement.getGeneratedKeys()) {
                    if (!keys.next()) {
                        throw new SQLException("No generated key returned for binding.");
                    }
                    bindingId = keys.getLong(1);
                }
            }

            connection.commit();
            return findActiveByDeviceId(deviceId);
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to bind device to vehicle.", exception);
        }
    }

    public VehicleDeviceBinding findActiveByDeviceId(long deviceId) {
        String sql = "SELECT * FROM vehicle_devices WHERE device_id = ? AND removed_at IS NULL ORDER BY installed_at DESC LIMIT 1";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, deviceId);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch active binding.", exception);
        }
    }

    private VehicleDeviceBinding map(ResultSet resultSet) throws SQLException {
        return new VehicleDeviceBinding(
                resultSet.getLong("id"),
                resultSet.getLong("vehicle_id"),
                resultSet.getLong("device_id"),
                resultSet.getString("installed_at"),
                resultSet.getString("removed_at")
        );
    }
}

package com.cst8411.finalproject.repository;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.MaintenanceRecord;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

public class MaintenanceRecordRepository {

    private final Database database;

    public MaintenanceRecordRepository(Database database) {
        this.database = database;
    }

    public MaintenanceRecord create(
            long customerId,
            long vehicleId,
            String serviceType,
            String description,
            double cost,
            String servicedAt
    ) {
        String sql = """
                INSERT INTO maintenance_records(customer_id, vehicle_id, service_type, description, cost, serviced_at)
                VALUES (?, ?, ?, ?, ?, ?)
                """;
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
            statement.setLong(1, customerId);
            statement.setLong(2, vehicleId);
            statement.setString(3, serviceType);
            statement.setString(4, description);
            statement.setDouble(5, cost);
            statement.setString(6, servicedAt);
            statement.executeUpdate();
            try (ResultSet keys = statement.getGeneratedKeys()) {
                if (!keys.next()) {
                    throw new SQLException("No generated key returned for maintenance record.");
                }
                return findById(keys.getLong(1));
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to create maintenance record.", exception);
        }
    }

    public MaintenanceRecord findById(long id) {
        String sql = "SELECT * FROM maintenance_records WHERE id = ?";
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, id);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next() ? map(resultSet) : null;
            }
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to fetch maintenance record.", exception);
        }
    }

    public List<MaintenanceRecord> listAll() {
        return listByClause("SELECT * FROM maintenance_records ORDER BY serviced_at DESC", null);
    }

    public List<MaintenanceRecord> listByCustomerId(long customerId) {
        return listByClause("SELECT * FROM maintenance_records WHERE customer_id = ? ORDER BY serviced_at DESC", customerId);
    }

    public List<MaintenanceRecord> listByVehicleId(long vehicleId) {
        return listByClause("SELECT * FROM maintenance_records WHERE vehicle_id = ? ORDER BY serviced_at DESC", vehicleId);
    }

    private List<MaintenanceRecord> listByClause(String sql, Long parameter) {
        List<MaintenanceRecord> records = new ArrayList<>();
        try (Connection connection = database.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            if (parameter != null) {
                statement.setLong(1, parameter);
            }
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    records.add(map(resultSet));
                }
            }
            return records;
        } catch (SQLException exception) {
            throw new RuntimeException("Unable to list maintenance records.", exception);
        }
    }

    private MaintenanceRecord map(ResultSet resultSet) throws SQLException {
        return new MaintenanceRecord(
                resultSet.getLong("id"),
                resultSet.getLong("customer_id"),
                resultSet.getLong("vehicle_id"),
                resultSet.getString("service_type"),
                resultSet.getString("description"),
                resultSet.getDouble("cost"),
                resultSet.getString("serviced_at"),
                resultSet.getString("created_at"),
                resultSet.getString("updated_at")
        );
    }
}

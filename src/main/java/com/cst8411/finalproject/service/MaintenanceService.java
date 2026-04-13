package com.cst8411.finalproject.service;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.MaintenanceRecord;
import com.cst8411.finalproject.domain.Vehicle;
import com.cst8411.finalproject.repository.CustomerRepository;
import com.cst8411.finalproject.repository.MaintenanceRecordRepository;
import com.cst8411.finalproject.repository.VehicleRepository;
import java.util.List;

public class MaintenanceService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public MaintenanceService(Database database) {
        this.customerRepository = new CustomerRepository(database);
        this.vehicleRepository = new VehicleRepository(database);
        this.maintenanceRecordRepository = new MaintenanceRecordRepository(database);
    }

    public MaintenanceRecord recordMaintenance(
            long customerId,
            long vehicleId,
            String serviceType,
            String description,
            double cost,
            String servicedAt
    ) {
        if (customerRepository.findById(customerId) == null) {
            throw new IllegalArgumentException("Customer not found.");
        }

        Vehicle vehicle = vehicleRepository.findById(vehicleId);
        if (vehicle == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }
        if (vehicle.customerId() != customerId) {
            throw new IllegalArgumentException("Vehicle does not belong to the specified customer.");
        }

        return maintenanceRecordRepository.create(customerId, vehicleId, serviceType, description, cost, servicedAt);
    }

    public List<MaintenanceRecord> listRecords() {
        return maintenanceRecordRepository.listAll();
    }

    public List<MaintenanceRecord> listByCustomer(long customerId) {
        return maintenanceRecordRepository.listByCustomerId(customerId);
    }
}

package com.cst8411.finalproject.service;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.DeviceOrder;
import com.cst8411.finalproject.repository.CustomerRepository;
import com.cst8411.finalproject.repository.DeviceOrderRepository;
import com.cst8411.finalproject.repository.VehicleRepository;
import java.util.List;

public class OrderService {

    private final CustomerRepository customerRepository;
    private final VehicleRepository vehicleRepository;
    private final DeviceOrderRepository deviceOrderRepository;

    public OrderService(Database database) {
        this.customerRepository = new CustomerRepository(database);
        this.vehicleRepository = new VehicleRepository(database);
        this.deviceOrderRepository = new DeviceOrderRepository(database);
    }

    public long recordPrepaidOrder(long customerId, Long vehicleId, String deviceName, String sku, double amountPaid) {
        if (customerRepository.findById(customerId) == null) {
            throw new IllegalArgumentException("Customer not found.");
        }
        if (vehicleId != null && vehicleRepository.findById(vehicleId) == null) {
            throw new IllegalArgumentException("Vehicle not found.");
        }

        return deviceOrderRepository.create(
                customerId,
                vehicleId,
                deviceName,
                sku,
                amountPaid,
                "PURCHASED_NOT_IN_STOCK"
        );
    }

    public List<DeviceOrder> listOrders() {
        return deviceOrderRepository.listAll();
    }
}

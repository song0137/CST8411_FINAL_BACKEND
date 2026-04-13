package com.cst8411.finalproject.service;

import com.cst8411.finalproject.db.Database;
import com.cst8411.finalproject.domain.AuthenticatedCustomer;
import com.cst8411.finalproject.domain.CustomerPortalResponse;
import com.cst8411.finalproject.domain.CustomerVehicleSnapshot;
import com.cst8411.finalproject.domain.Vehicle;
import com.cst8411.finalproject.repository.DeviceOrderRepository;
import com.cst8411.finalproject.repository.DeviceRepository;
import com.cst8411.finalproject.repository.MaintenanceRecordRepository;
import com.cst8411.finalproject.repository.VehicleRepository;
import java.util.ArrayList;
import java.util.List;

public class CustomerPortalService {

    private final AuthService authService;
    private final VehicleRepository vehicleRepository;
    private final DeviceRepository deviceRepository;
    private final DeviceOrderRepository deviceOrderRepository;
    private final MaintenanceRecordRepository maintenanceRecordRepository;

    public CustomerPortalService(Database database) {
        this.authService = new AuthService(database);
        this.vehicleRepository = new VehicleRepository(database);
        this.deviceRepository = new DeviceRepository(database);
        this.deviceOrderRepository = new DeviceOrderRepository(database);
        this.maintenanceRecordRepository = new MaintenanceRecordRepository(database);
    }

    public CustomerPortalResponse getPortal(String token) {
        AuthenticatedCustomer authenticatedCustomer = authService.authenticate(token);

        List<CustomerVehicleSnapshot> snapshots = new ArrayList<>();
        for (Vehicle vehicle : vehicleRepository.listByCustomerId(authenticatedCustomer.customer().id())) {
            snapshots.add(new CustomerVehicleSnapshot(
                    vehicle,
                    deviceRepository.listByVehicleId(vehicle.id()),
                    maintenanceRecordRepository.listByVehicleId(vehicle.id())
            ));
        }

        return new CustomerPortalResponse(
                authenticatedCustomer.customer(),
                authenticatedCustomer.account(),
                snapshots,
                deviceOrderRepository.listByCustomerId(authenticatedCustomer.customer().id())
        );
    }
}

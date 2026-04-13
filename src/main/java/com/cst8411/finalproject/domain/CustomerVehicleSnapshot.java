package com.cst8411.finalproject.domain;

import java.util.List;

public record CustomerVehicleSnapshot(
        Vehicle vehicle,
        List<Device> devices,
        List<MaintenanceRecord> maintenanceRecords
) {
}

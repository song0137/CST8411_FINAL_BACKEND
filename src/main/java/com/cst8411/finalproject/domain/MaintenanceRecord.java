package com.cst8411.finalproject.domain;

public record MaintenanceRecord(
        long id,
        long customerId,
        long vehicleId,
        String serviceType,
        String description,
        double cost,
        String servicedAt,
        String createdAt,
        String updatedAt
) {
}

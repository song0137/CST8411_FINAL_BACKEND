package com.cst8411.finalproject.domain;

public record DeviceOrder(
        long id,
        long customerId,
        Long vehicleId,
        String deviceName,
        String sku,
        double amountPaid,
        String status,
        String createdAt,
        String updatedAt
) {
}

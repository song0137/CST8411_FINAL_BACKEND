package com.cst8411.finalproject.domain;

public record Device(
        long id,
        String deviceName,
        String sku,
        String serialNumber,
        String status,
        String createdAt,
        String updatedAt
) {
}

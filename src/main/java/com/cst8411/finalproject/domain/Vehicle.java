package com.cst8411.finalproject.domain;

public record Vehicle(
        long id,
        long customerId,
        String vin,
        String make,
        String model,
        int year,
        String licensePlate,
        String createdAt,
        String updatedAt
) {
}

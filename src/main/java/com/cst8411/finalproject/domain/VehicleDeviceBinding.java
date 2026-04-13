package com.cst8411.finalproject.domain;

public record VehicleDeviceBinding(
        long id,
        long vehicleId,
        long deviceId,
        String installedAt,
        String removedAt
) {
}

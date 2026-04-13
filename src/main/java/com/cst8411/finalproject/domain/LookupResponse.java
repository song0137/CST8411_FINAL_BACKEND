package com.cst8411.finalproject.domain;

import java.util.List;

public record LookupResponse(
        Customer customer,
        Vehicle vehicle,
        Device device,
        List<Device> devices,
        List<DeviceOrder> orders
) {
}

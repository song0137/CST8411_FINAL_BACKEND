package com.cst8411.finalproject.domain;

import java.util.List;

public record CustomerPortalResponse(
        Customer customer,
        CustomerAccount account,
        List<CustomerVehicleSnapshot> vehicles,
        List<DeviceOrder> orders
) {
}

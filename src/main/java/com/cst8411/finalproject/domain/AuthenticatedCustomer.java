package com.cst8411.finalproject.domain;

public record AuthenticatedCustomer(
        CustomerAccount account,
        Customer customer
) {
}

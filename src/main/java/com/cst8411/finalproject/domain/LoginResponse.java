package com.cst8411.finalproject.domain;

public record LoginResponse(
        String token,
        long customerId,
        String customerName,
        String username
) {
}

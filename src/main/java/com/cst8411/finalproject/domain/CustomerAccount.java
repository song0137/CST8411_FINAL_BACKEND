package com.cst8411.finalproject.domain;

public record CustomerAccount(
        long id,
        long customerId,
        String username,
        String passwordHash,
        String createdAt,
        String updatedAt
) {
}

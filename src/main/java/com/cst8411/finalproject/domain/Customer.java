package com.cst8411.finalproject.domain;

public record Customer(
        long id,
        String fullName,
        String phone,
        String email,
        String createdAt,
        String updatedAt
) {
}

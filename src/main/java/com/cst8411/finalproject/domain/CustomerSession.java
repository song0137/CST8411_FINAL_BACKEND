package com.cst8411.finalproject.domain;

public record CustomerSession(
        String token,
        long customerAccountId,
        String expiresAt,
        String createdAt
) {
}

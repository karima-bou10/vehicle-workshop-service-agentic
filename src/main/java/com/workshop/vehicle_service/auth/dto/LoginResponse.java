package com.workshop.vehicle_service.auth.dto;

public record LoginResponse(
        String token,
        String type,
        long expiresIn
) {
}

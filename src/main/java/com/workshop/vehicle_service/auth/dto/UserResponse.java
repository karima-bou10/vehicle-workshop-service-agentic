package com.workshop.vehicle_service.auth.dto;

public record UserResponse(
        String username,
        String role
) {
}

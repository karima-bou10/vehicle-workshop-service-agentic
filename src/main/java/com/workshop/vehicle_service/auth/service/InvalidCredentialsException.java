package com.workshop.vehicle_service.auth.service;

import org.springframework.security.core.AuthenticationException;

public class InvalidCredentialsException extends AuthenticationException {
    public InvalidCredentialsException(String message) {
        super(message);
    }
}

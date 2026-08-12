package com.workshop.vehicle_service.common.exception;

public class TransitionIllegaleException extends RuntimeException {
    public TransitionIllegaleException(String message) {
        super(message);
    }
}

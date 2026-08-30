package com.workshop.vehicle_service.vehicule.service.impl;

// Must extend RuntimeException (not Throwable) so Spring MVC's @ExceptionHandler actually catches it.
public class BusinessException extends RuntimeException {
    public BusinessException(String s) {
        super(s);
    }
}

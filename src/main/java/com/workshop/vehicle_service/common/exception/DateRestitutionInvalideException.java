package com.workshop.vehicle_service.common.exception;

/** dateRestitutionPrevue antérieure à dateDepot (création ou édition). */
public class DateRestitutionInvalideException extends RuntimeException {

    public DateRestitutionInvalideException(String message) {
        super(message);
    }
}

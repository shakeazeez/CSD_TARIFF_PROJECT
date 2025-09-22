package com.tariff.calculation.tariffCalc.exception;

public class ApiFailureException extends RuntimeException {
    public ApiFailureException(String message) {
        super(message);
    }

    public ApiFailureException(String message, Throwable cause) {
        super(message, cause);
    }
}
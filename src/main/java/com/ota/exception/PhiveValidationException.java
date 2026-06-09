package com.ota.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
public class PhiveValidationException extends RuntimeException {
    public PhiveValidationException(String message, Throwable cause) {
        super(message, cause);
    }
}

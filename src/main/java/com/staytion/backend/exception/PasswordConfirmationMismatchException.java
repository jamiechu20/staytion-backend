package com.staytion.backend.exception;

public class PasswordConfirmationMismatchException extends RuntimeException {
    public PasswordConfirmationMismatchException(String message) {
        super(message);
    }
    public PasswordConfirmationMismatchException(String message, Throwable cause) {super(message, cause);}
}

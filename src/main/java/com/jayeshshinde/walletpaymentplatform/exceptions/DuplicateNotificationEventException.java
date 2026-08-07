package com.jayeshshinde.walletpaymentplatform.exceptions;

public class DuplicateNotificationEventException extends RuntimeException {
    public DuplicateNotificationEventException(String message) {
        super("Duplicate notification event found " + message);
    }
}

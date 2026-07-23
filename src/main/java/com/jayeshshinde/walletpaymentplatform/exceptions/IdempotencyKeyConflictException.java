package com.jayeshshinde.walletpaymentplatform.exceptions;

public class IdempotencyKeyConflictException extends RuntimeException {
    public IdempotencyKeyConflictException(String message) {
        super(message);
    }
}

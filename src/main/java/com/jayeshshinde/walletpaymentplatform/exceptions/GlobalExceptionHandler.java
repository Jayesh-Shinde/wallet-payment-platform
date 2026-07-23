package com.jayeshshinde.walletpaymentplatform.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ReplayNotReadyException.class)
    public ResponseEntity<Object> handleReplayNotReadyException(ReplayNotReadyException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("timestamp", LocalDateTime.now());
        body.put("status", HttpStatus.CONFLICT.value());
        body.put("error", "Conflicts");
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).header("Retry-After", String.valueOf(3)).body(body);
    }
}

package com.jayeshshinde.walletpaymentplatform.component.interceptors;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.UUID;

@Component
public class IdempotencyInterceptor implements HandlerInterceptor {
    private static final String IdempotencyKey = "X-Idempotency-Key";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {
        String headerValue = request.getHeader(IdempotencyKey);
        if (headerValue == null || headerValue.isEmpty()) {
            return true;
        }
        try {
            UUID idempotencyKey = UUID.fromString(headerValue);
            request.setAttribute(IdempotencyKey, idempotencyKey);
        } catch (IllegalArgumentException e) {
            response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Invalid Idempotency Key");
            return false;
        }
        return true;
    }

}

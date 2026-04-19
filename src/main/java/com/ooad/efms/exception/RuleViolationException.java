package com.ooad.efms.exception;

/**
 * Thrown by any link in the approval rule chain (or submission rule checks)
 * when a category rule or approval-limit constraint is violated. Mapped to
 * HTTP 422 by GlobalExceptionHandler so the client can distinguish rule
 * violations from plain bad input.
 */
public class RuleViolationException extends RuntimeException {

    private final String code;

    public RuleViolationException(String code, String message) {
        super(message);
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}

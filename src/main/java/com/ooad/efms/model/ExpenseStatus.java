package com.ooad.efms.model;

/**
 * Lifecycle of a submitted expense claim.
 * PENDING_APPROVAL -> (APPROVED | REJECTED)
 */
public enum ExpenseStatus {
    PENDING_APPROVAL,
    APPROVED,
    REJECTED
}

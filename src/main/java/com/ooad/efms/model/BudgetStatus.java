package com.ooad.efms.model;

/**
 * Lifecycle states of a Budget aggregate.
 * Transitions: DRAFT -> READY -> SUBMITTED -> (APPROVED | REJECTED)
 * A budget is created in DRAFT, marked READY once validation passes with no
 * blocking alerts, then moves to SUBMITTED when the Organizer hands it off.
 */
public enum BudgetStatus {
    DRAFT,
    READY,
    SUBMITTED,
    APPROVED,
    REJECTED
}

package com.ooad.efms.model;

/**
 * Required approval chain for a submitted expense, assigned at submission
 * time by the active {@code ApprovalRoutingStrategy} (minor use case:
 * Role-Based Approval Flow).
 *
 *  LEVEL_1         -> single-level approval (e.g. Faculty Coordinator only)
 *  LEVEL_1_AND_2   -> two-step approval (e.g. Faculty Coordinator + Finance Committee)
 */
public enum ApprovalLevel {
    LEVEL_1,
    LEVEL_1_AND_2
}

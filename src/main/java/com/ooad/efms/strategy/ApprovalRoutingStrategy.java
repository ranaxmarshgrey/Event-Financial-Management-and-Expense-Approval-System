package com.ooad.efms.strategy;

import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.Expense;

/**
 * Strategy Pattern — Role-Based Approval Flow (minor use case).
 *
 * Decides which approval chain a submitted expense must traverse. Different
 * institutions route approvals differently:
 *   - by amount        (small vs. large expenses)
 *   - by category type (e.g. anything under "Prizes" always needs finance sign-off)
 *   - by event type    (sponsored vs. internal)
 *
 * ExpenseService depends only on this interface (Dependency Inversion), so
 * policy changes are a configuration swap, not a code change (Open/Closed).
 */
public interface ApprovalRoutingStrategy {
    ApprovalLevel routeFor(Expense expense);
    String name();
}

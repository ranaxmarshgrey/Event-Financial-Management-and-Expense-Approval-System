package com.ooad.efms.strategy;

import com.ooad.efms.model.Budget;

/**
 * Strategy Pattern — approval policy for a submitted budget.
 *
 * Different colleges / finance departments may want different rules:
 *   - auto-approve small budgets, manual review for large ones
 *   - require manual review for every budget
 *   - (future) require multiple sign-offs above a higher threshold
 *
 * BudgetService depends only on this interface (Dependency Inversion),
 * so swapping the policy is a configuration change, not a code change.
 */
public interface ApprovalStrategy {
    ApprovalDecision evaluate(Budget budget);
    String name();
}

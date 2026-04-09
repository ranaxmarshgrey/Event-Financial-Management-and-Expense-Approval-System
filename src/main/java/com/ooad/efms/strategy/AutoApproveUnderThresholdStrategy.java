package com.ooad.efms.strategy;

import com.ooad.efms.model.Budget;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: auto-approves budgets below a threshold,
 * forwards larger budgets to a human approver.
 *
 * Marked @Primary so Spring injects this by default when BudgetService
 * asks for an ApprovalStrategy. Swap to ManualApprovalStrategy by moving
 * @Primary there — no other code changes needed (Open/Closed Principle).
 */
@Component
@Primary
public class AutoApproveUnderThresholdStrategy implements ApprovalStrategy {

    private static final BigDecimal THRESHOLD = new BigDecimal("10000");

    @Override
    public ApprovalDecision evaluate(Budget budget) {
        if (budget.getTotalLimit().compareTo(THRESHOLD) <= 0) {
            return new ApprovalDecision(
                    ApprovalDecision.Outcome.AUTO_APPROVED,
                    "Budget under auto-approval threshold of " + THRESHOLD,
                    name());
        }
        return new ApprovalDecision(
                ApprovalDecision.Outcome.REQUIRES_MANUAL_REVIEW,
                "Budget exceeds auto-approval threshold of " + THRESHOLD + "; forwarded to Approving Authority",
                name());
    }

    @Override
    public String name() {
        return "AUTO_UNDER_THRESHOLD";
    }
}

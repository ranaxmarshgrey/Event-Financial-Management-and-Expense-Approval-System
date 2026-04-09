package com.ooad.efms.strategy;

import com.ooad.efms.model.Budget;
import org.springframework.stereotype.Component;

/**
 * Concrete Strategy: every budget must be reviewed manually,
 * regardless of amount. Useful for strict compliance environments.
 */
@Component
public class ManualApprovalStrategy implements ApprovalStrategy {

    @Override
    public ApprovalDecision evaluate(Budget budget) {
        return new ApprovalDecision(
                ApprovalDecision.Outcome.REQUIRES_MANUAL_REVIEW,
                "Policy requires manual review for every budget",
                name());
    }

    @Override
    public String name() {
        return "MANUAL_ONLY";
    }
}

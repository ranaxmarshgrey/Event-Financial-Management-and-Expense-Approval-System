package com.ooad.efms.strategy;

import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.Expense;
import org.springframework.stereotype.Component;

import java.util.Set;

/**
 * Alternative Concrete Strategy: routes based on the expense category name.
 * Categories flagged as "sensitive" always need two-step approval regardless
 * of amount; everything else uses single-step.
 *
 * Not marked @Primary — activated by moving @Primary here.
 */
@Component
public class CategoryBasedRoutingStrategy implements ApprovalRoutingStrategy {

    private static final Set<String> SENSITIVE_CATEGORIES = Set.of("Prizes", "Honorarium", "Sponsorship");

    @Override
    public ApprovalLevel routeFor(Expense expense) {
        String name = expense.getCategory().getName();
        if (SENSITIVE_CATEGORIES.contains(name)) {
            return ApprovalLevel.LEVEL_1_AND_2;
        }
        return ApprovalLevel.LEVEL_1;
    }

    @Override
    public String name() {
        return "CATEGORY_BASED";
    }
}

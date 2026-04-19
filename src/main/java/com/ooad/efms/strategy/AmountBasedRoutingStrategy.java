package com.ooad.efms.strategy;

import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.Expense;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Concrete Strategy: routes based on the expense amount.
 *   amount <= THRESHOLD  -> LEVEL_1   (single-step approval)
 *   amount >  THRESHOLD  -> LEVEL_1_AND_2 (two-step approval)
 *
 * Marked @Primary so Spring injects this by default. Swap policies by moving
 * @Primary to {@link CategoryBasedRoutingStrategy} — no service-layer edit.
 */
@Component
@Primary
public class AmountBasedRoutingStrategy implements ApprovalRoutingStrategy {

    private static final BigDecimal THRESHOLD = new BigDecimal("2000");

    @Override
    public ApprovalLevel routeFor(Expense expense) {
        if (expense.getAmount().compareTo(THRESHOLD) <= 0) {
            return ApprovalLevel.LEVEL_1;
        }
        return ApprovalLevel.LEVEL_1_AND_2;
    }

    @Override
    public String name() {
        return "AMOUNT_BASED";
    }
}

package com.ooad.efms.service.rules;

import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.CategoryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Chain link #3 — per-expense cap defined by the category rule (minor UC #3). */
@Component
@Order(30)
public class MaxAmountRule implements ApprovalRule {

    @Override
    public void check(ApprovalContext ctx) {
        CategoryRule rule = ctx.getExpense().getCategory().getRule();
        if (rule == null || rule.getMaxExpenseAmount() == null) return;
        if (ctx.getExpense().getAmount().compareTo(rule.getMaxExpenseAmount()) > 0) {
            throw new RuleViolationException(
                    "EXCEEDS_CATEGORY_MAX",
                    "Expense amount " + ctx.getExpense().getAmount()
                            + " exceeds category cap of " + rule.getMaxExpenseAmount());
        }
    }
}

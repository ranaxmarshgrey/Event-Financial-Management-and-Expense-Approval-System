package com.ooad.efms.service.rules;

import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.CategoryRule;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Chain link #2 — Finance Admin can block a category (minor UC #3). */
@Component
@Order(20)
public class BlockedCategoryRule implements ApprovalRule {

    @Override
    public void check(ApprovalContext ctx) {
        CategoryRule rule = ctx.getExpense().getCategory().getRule();
        if (rule != null && rule.isBlocked()) {
            throw new RuleViolationException(
                    "CATEGORY_BLOCKED",
                    "Category '" + ctx.getExpense().getCategory().getName() + "' is blocked for new approvals");
        }
    }
}

package com.ooad.efms.service.rules;

import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.ExpenseStatus;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Chain link #1 — terminal states (APPROVED/REJECTED) cannot be re-actioned. */
@Component
@Order(10)
public class NotPendingRule implements ApprovalRule {

    @Override
    public void check(ApprovalContext ctx) {
        if (ctx.getExpense().getStatus() != ExpenseStatus.PENDING_APPROVAL) {
            throw new RuleViolationException(
                    "EXPENSE_NOT_PENDING",
                    "Expense is already " + ctx.getExpense().getStatus() + " and cannot be re-actioned");
        }
    }
}

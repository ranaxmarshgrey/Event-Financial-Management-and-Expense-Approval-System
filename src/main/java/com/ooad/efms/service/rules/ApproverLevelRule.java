package com.ooad.efms.service.rules;

import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.Expense;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/**
 * Chain link #4 — enforces the required approval chain assigned at submit
 * time. LEVEL_1 needs exactly one L1 approval. LEVEL_1_AND_2 needs L1 first,
 * then L2 — approvers cannot act out of order or at the wrong level.
 */
@Component
@Order(40)
public class ApproverLevelRule implements ApprovalRule {

    @Override
    public void check(ApprovalContext ctx) {
        Expense expense = ctx.getExpense();
        ApprovalRole approverRole = ctx.getApprover().getRole();
        if (approverRole == null) {
            throw new RuleViolationException(
                    "APPROVER_ROLE_UNSET", "Approver has no role configured");
        }

        if (expense.getRequiredApprovalLevel() == ApprovalLevel.LEVEL_1) {
            if (approverRole != ApprovalRole.L1) {
                throw new RuleViolationException(
                        "WRONG_APPROVER_LEVEL",
                        "Expense requires a LEVEL_1 approver but got " + approverRole);
            }
            return;
        }

        // LEVEL_1_AND_2: L1 must go first, then L2.
        boolean l1Done = ctx.hasApprovalAt(ApprovalRole.L1);
        if (!l1Done && approverRole != ApprovalRole.L1) {
            throw new RuleViolationException(
                    "L1_APPROVAL_FIRST",
                    "Expense needs L1 approval before L2 can act");
        }
        if (l1Done && approverRole != ApprovalRole.L2) {
            throw new RuleViolationException(
                    "ALREADY_L1_APPROVED",
                    "L1 approval is already recorded; this step requires an L2 approver");
        }
    }
}

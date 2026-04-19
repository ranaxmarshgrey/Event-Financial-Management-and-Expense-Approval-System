package com.ooad.efms.service.rules;

import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ApprovingAuthority;
import com.ooad.efms.model.ApprovalLevel;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

/** Chain link #5 — an approver cannot green-light amounts above their personal limit
 *  when they are the FINAL signer on the claim. L1 co-signing a LEVEL_1_AND_2
 *  expense is not the final decision (L2 still has to approve), so L1's personal
 *  limit is not enforced in that case. */
@Component
@Order(50)
public class ApprovalLimitRule implements ApprovalRule {

    @Override
    public void check(ApprovalContext ctx) {
        ApprovingAuthority approver = ctx.getApprover();
        if (approver.getApprovalLimit() == null) return;

        ApprovalLevel level = ctx.getExpense().getRequiredApprovalLevel();
        boolean approverIsFinalSigner =
                level == ApprovalLevel.LEVEL_1 && approver.getRole() == ApprovalRole.L1
             || level == ApprovalLevel.LEVEL_1_AND_2 && approver.getRole() == ApprovalRole.L2;
        if (!approverIsFinalSigner) return;

        if (ctx.getExpense().getAmount().compareTo(approver.getApprovalLimit()) > 0) {
            throw new RuleViolationException(
                    "ABOVE_APPROVER_LIMIT",
                    "Expense amount " + ctx.getExpense().getAmount()
                            + " exceeds approver's personal limit of " + approver.getApprovalLimit());
        }
    }
}

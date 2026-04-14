package com.ooad.efms.service.rules;

import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ApprovingAuthority;
import com.ooad.efms.model.Expense;
import com.ooad.efms.model.ExpenseApproval;

import java.util.List;

/**
 * Immutable input to every {@link ApprovalRule} in the chain. Carries the
 * expense being approved, the acting approver, and the prior approval
 * history (used by ApproverLevelRule to enforce L1-before-L2 ordering).
 */
public final class ApprovalContext {

    private final Expense expense;
    private final ApprovingAuthority approver;
    private final List<ExpenseApproval> priorApprovals;

    public ApprovalContext(Expense expense,
                           ApprovingAuthority approver,
                           List<ExpenseApproval> priorApprovals) {
        this.expense = expense;
        this.approver = approver;
        this.priorApprovals = priorApprovals;
    }

    public Expense getExpense() { return expense; }
    public ApprovingAuthority getApprover() { return approver; }
    public List<ExpenseApproval> getPriorApprovals() { return priorApprovals; }

    /** True if the chain already recorded an approval at the given level. */
    public boolean hasApprovalAt(ApprovalRole role) {
        return priorApprovals.stream()
                .anyMatch(a -> a.getApproverRoleAtAction() == role
                        && a.getAction() == com.ooad.efms.model.ApprovalAction.APPROVE);
    }
}

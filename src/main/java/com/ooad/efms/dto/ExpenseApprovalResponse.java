package com.ooad.efms.dto;

import com.ooad.efms.model.ApprovalAction;
import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ExpenseApproval;

import java.time.Instant;

public class ExpenseApprovalResponse {

    private Long id;
    private Long expenseId;
    private String approverName;
    private ApprovalRole approverRole;
    private ApprovalAction action;
    private String notes;
    private Instant actedAt;

    public static ExpenseApprovalResponse from(ExpenseApproval a) {
        ExpenseApprovalResponse r = new ExpenseApprovalResponse();
        r.id = a.getId();
        r.expenseId = a.getExpense().getId();
        r.approverName = a.getApprover().getName();
        r.approverRole = a.getApproverRoleAtAction();
        r.action = a.getAction();
        r.notes = a.getNotes();
        r.actedAt = a.getActedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getExpenseId() { return expenseId; }
    public String getApproverName() { return approverName; }
    public ApprovalRole getApproverRole() { return approverRole; }
    public ApprovalAction getAction() { return action; }
    public String getNotes() { return notes; }
    public Instant getActedAt() { return actedAt; }
}

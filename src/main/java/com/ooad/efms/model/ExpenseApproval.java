package com.ooad.efms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.time.Instant;

/**
 * Audit record for a single approve/reject action on an expense.
 * LEVEL_1_AND_2 expenses accumulate two APPROVE records (one L1, one L2)
 * before the parent Expense transitions to APPROVED.
 */
@Entity
@Table(name = "expense_approvals")
public class ExpenseApproval {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "expense_id", nullable = false)
    @JsonIgnore
    private Expense expense;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "approver_id", nullable = false)
    private ApprovingAuthority approver;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalRole approverRoleAtAction;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalAction action;

    @Column(length = 500)
    private String notes;

    @Column(nullable = false, updatable = false)
    private Instant actedAt = Instant.now();

    public ExpenseApproval() {}

    public ExpenseApproval(Expense expense, ApprovingAuthority approver, ApprovalAction action, String notes) {
        this.expense = expense;
        this.approver = approver;
        this.approverRoleAtAction = approver.getRole();
        this.action = action;
        this.notes = notes;
    }

    public Long getId() { return id; }
    public Expense getExpense() { return expense; }
    public void setExpense(Expense expense) { this.expense = expense; }
    public ApprovingAuthority getApprover() { return approver; }
    public void setApprover(ApprovingAuthority approver) { this.approver = approver; }
    public ApprovalRole getApproverRoleAtAction() { return approverRoleAtAction; }
    public void setApproverRoleAtAction(ApprovalRole approverRoleAtAction) { this.approverRoleAtAction = approverRoleAtAction; }
    public ApprovalAction getAction() { return action; }
    public void setAction(ApprovalAction action) { this.action = action; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
    public Instant getActedAt() { return actedAt; }
}

package com.ooad.efms.dto;

import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.Expense;
import com.ooad.efms.model.ExpenseStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

public class ExpenseResponse {

    private Long id;
    private Long categoryId;
    private String categoryName;
    private Long budgetId;
    private String description;
    private BigDecimal amount;
    private LocalDate expenseDate;
    private String supportingDocUrl;
    private ExpenseStatus status;
    private ApprovalLevel requiredApprovalLevel;
    private String submittedBy;
    private Instant submittedAt;

    public static ExpenseResponse from(Expense e) {
        ExpenseResponse r = new ExpenseResponse();
        r.id = e.getId();
        r.categoryId = e.getCategory().getId();
        r.categoryName = e.getCategory().getName();
        r.budgetId = e.getCategory().getBudget().getId();
        r.description = e.getDescription();
        r.amount = e.getAmount();
        r.expenseDate = e.getExpenseDate();
        r.supportingDocUrl = e.getSupportingDocUrl();
        r.status = e.getStatus();
        r.requiredApprovalLevel = e.getRequiredApprovalLevel();
        r.submittedBy = e.getSubmittedBy().getName();
        r.submittedAt = e.getSubmittedAt();
        return r;
    }

    public Long getId() { return id; }
    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public Long getBudgetId() { return budgetId; }
    public String getDescription() { return description; }
    public BigDecimal getAmount() { return amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public String getSupportingDocUrl() { return supportingDocUrl; }
    public ExpenseStatus getStatus() { return status; }
    public ApprovalLevel getRequiredApprovalLevel() { return requiredApprovalLevel; }
    public String getSubmittedBy() { return submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
}

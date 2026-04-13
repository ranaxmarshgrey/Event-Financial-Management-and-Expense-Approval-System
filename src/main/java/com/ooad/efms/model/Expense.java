package com.ooad.efms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "expenses")
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal amount;

    @Column(nullable = false)
    private LocalDate expenseDate;

    /** Optional URL/reference to an uploaded receipt or bill. */
    private String supportingDocUrl;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ExpenseStatus status = ExpenseStatus.PENDING_APPROVAL;

    /** Required approval chain — stamped by ApprovalRoutingStrategy at submit time. */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ApprovalLevel requiredApprovalLevel;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    @JsonIgnore
    private ExpenseCategory category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "submitted_by_id", nullable = false)
    private Organizer submittedBy;

    @Column(nullable = false, updatable = false)
    private Instant submittedAt = Instant.now();

    public Expense() {}

    public Expense(String description,
                   BigDecimal amount,
                   LocalDate expenseDate,
                   String supportingDocUrl,
                   ExpenseCategory category,
                   Organizer submittedBy,
                   ApprovalLevel requiredApprovalLevel) {
        this.description = description;
        this.amount = amount;
        this.expenseDate = expenseDate;
        this.supportingDocUrl = supportingDocUrl;
        this.category = category;
        this.submittedBy = submittedBy;
        this.requiredApprovalLevel = requiredApprovalLevel;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public LocalDate getExpenseDate() { return expenseDate; }
    public void setExpenseDate(LocalDate expenseDate) { this.expenseDate = expenseDate; }
    public String getSupportingDocUrl() { return supportingDocUrl; }
    public void setSupportingDocUrl(String supportingDocUrl) { this.supportingDocUrl = supportingDocUrl; }
    public ExpenseStatus getStatus() { return status; }
    public void setStatus(ExpenseStatus status) { this.status = status; }
    public ApprovalLevel getRequiredApprovalLevel() { return requiredApprovalLevel; }
    public void setRequiredApprovalLevel(ApprovalLevel requiredApprovalLevel) { this.requiredApprovalLevel = requiredApprovalLevel; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
    public Organizer getSubmittedBy() { return submittedBy; }
    public void setSubmittedBy(Organizer submittedBy) { this.submittedBy = submittedBy; }
    public Instant getSubmittedAt() { return submittedAt; }
    public void setSubmittedAt(Instant submittedAt) { this.submittedAt = submittedAt; }
}

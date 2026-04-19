package com.ooad.efms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * Minor use case #3 — Expense Category Rules.
 *
 * Rules attached to a single ExpenseCategory. Enforced both at submission
 * time (by ExpenseService) and at approval time (by the rule chain).
 *
 *   maxExpenseAmount         — hard cap on a single expense (nullable = no cap)
 *   requiresL2ApprovalAbove  — amounts strictly above this always need L1+L2
 *                              regardless of the routing strategy (nullable = off)
 *   blocked                  — if true, no new expenses may be submitted or
 *                              approved against this category
 */
@Entity
@Table(name = "category_rules")
public class CategoryRule {

    @Id
    private Long categoryId;

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "category_id")
    @JsonIgnore
    private ExpenseCategory category;

    @Column(precision = 12, scale = 2)
    private BigDecimal maxExpenseAmount;

    @Column(precision = 12, scale = 2)
    private BigDecimal requiresL2ApprovalAbove;

    @Column(nullable = false)
    private boolean blocked = false;

    public CategoryRule() {}

    public CategoryRule(ExpenseCategory category) {
        this.category = category;
    }

    public Long getCategoryId() { return categoryId; }
    public void setCategoryId(Long categoryId) { this.categoryId = categoryId; }
    public ExpenseCategory getCategory() { return category; }
    public void setCategory(ExpenseCategory category) { this.category = category; }
    public BigDecimal getMaxExpenseAmount() { return maxExpenseAmount; }
    public void setMaxExpenseAmount(BigDecimal maxExpenseAmount) { this.maxExpenseAmount = maxExpenseAmount; }
    public BigDecimal getRequiresL2ApprovalAbove() { return requiresL2ApprovalAbove; }
    public void setRequiresL2ApprovalAbove(BigDecimal requiresL2ApprovalAbove) { this.requiresL2ApprovalAbove = requiresL2ApprovalAbove; }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
}

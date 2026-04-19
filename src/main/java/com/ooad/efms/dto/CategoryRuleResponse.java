package com.ooad.efms.dto;

import com.ooad.efms.model.CategoryRule;

import java.math.BigDecimal;

public class CategoryRuleResponse {

    private Long categoryId;
    private String categoryName;
    private BigDecimal maxExpenseAmount;
    private BigDecimal requiresL2ApprovalAbove;
    private boolean blocked;

    public static CategoryRuleResponse from(CategoryRule r) {
        CategoryRuleResponse out = new CategoryRuleResponse();
        out.categoryId = r.getCategory().getId();
        out.categoryName = r.getCategory().getName();
        out.maxExpenseAmount = r.getMaxExpenseAmount();
        out.requiresL2ApprovalAbove = r.getRequiresL2ApprovalAbove();
        out.blocked = r.isBlocked();
        return out;
    }

    public Long getCategoryId() { return categoryId; }
    public String getCategoryName() { return categoryName; }
    public BigDecimal getMaxExpenseAmount() { return maxExpenseAmount; }
    public BigDecimal getRequiresL2ApprovalAbove() { return requiresL2ApprovalAbove; }
    public boolean isBlocked() { return blocked; }
}

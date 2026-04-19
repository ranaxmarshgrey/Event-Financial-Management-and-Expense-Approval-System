package com.ooad.efms.dto;

import java.math.BigDecimal;

public class SetCategoryRuleRequest {

    private BigDecimal maxExpenseAmount;
    private BigDecimal requiresL2ApprovalAbove;
    private Boolean blocked;

    public BigDecimal getMaxExpenseAmount() { return maxExpenseAmount; }
    public void setMaxExpenseAmount(BigDecimal maxExpenseAmount) { this.maxExpenseAmount = maxExpenseAmount; }
    public BigDecimal getRequiresL2ApprovalAbove() { return requiresL2ApprovalAbove; }
    public void setRequiresL2ApprovalAbove(BigDecimal requiresL2ApprovalAbove) { this.requiresL2ApprovalAbove = requiresL2ApprovalAbove; }
    public Boolean getBlocked() { return blocked; }
    public void setBlocked(Boolean blocked) { this.blocked = blocked; }
}

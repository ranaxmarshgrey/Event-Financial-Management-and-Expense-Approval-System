package com.ooad.efms.model;

import jakarta.persistence.*;

import java.math.BigDecimal;

/**
 * A reviewer who can approve or reject expense claims. Has an approval
 * level (L1/L2) and a hard per-expense limit — the chain-of-responsibility
 * rule pipeline enforces both at approval time.
 */
@Entity
@DiscriminatorValue("APPROVING_AUTHORITY")
public class ApprovingAuthority extends User {

    @Enumerated(EnumType.STRING)
    @Column(name = "approval_role")
    private ApprovalRole role;

    @Column(name = "approval_limit", precision = 12, scale = 2)
    private BigDecimal approvalLimit;

    public ApprovingAuthority() { super(); }

    public ApprovingAuthority(String name, String email, ApprovalRole role, BigDecimal approvalLimit) {
        super(name, email);
        this.role = role;
        this.approvalLimit = approvalLimit;
    }

    @Override
    public String getRoleName() {
        return "APPROVING_AUTHORITY";
    }

    public ApprovalRole getRole() { return role; }
    public void setRole(ApprovalRole role) { this.role = role; }
    public BigDecimal getApprovalLimit() { return approvalLimit; }
    public void setApprovalLimit(BigDecimal approvalLimit) { this.approvalLimit = approvalLimit; }
}

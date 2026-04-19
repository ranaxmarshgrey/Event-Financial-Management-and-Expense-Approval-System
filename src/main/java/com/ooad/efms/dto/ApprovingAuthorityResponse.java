package com.ooad.efms.dto;

import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ApprovingAuthority;

import java.math.BigDecimal;

public class ApprovingAuthorityResponse {

    private Long id;
    private String name;
    private String email;
    private ApprovalRole role;
    private BigDecimal approvalLimit;

    public static ApprovingAuthorityResponse from(ApprovingAuthority a) {
        ApprovingAuthorityResponse r = new ApprovingAuthorityResponse();
        r.id = a.getId();
        r.name = a.getName();
        r.email = a.getEmail();
        r.role = a.getRole();
        r.approvalLimit = a.getApprovalLimit();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public String getEmail() { return email; }
    public ApprovalRole getRole() { return role; }
    public BigDecimal getApprovalLimit() { return approvalLimit; }
}

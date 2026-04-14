package com.ooad.efms.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class RejectExpenseRequest {

    @NotNull
    private Long approverId;

    @NotBlank
    private String reason;

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

package com.ooad.efms.dto;

import jakarta.validation.constraints.NotNull;

public class ApproveExpenseRequest {

    @NotNull
    private Long approverId;

    private String notes;

    public Long getApproverId() { return approverId; }
    public void setApproverId(Long approverId) { this.approverId = approverId; }
    public String getNotes() { return notes; }
    public void setNotes(String notes) { this.notes = notes; }
}

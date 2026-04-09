package com.ooad.efms.dto;

/**
 * Alert produced by VarianceAlertService during budget validation.
 * ERROR severity = blocking (cannot submit); WARNING = advisory.
 */
public class AlertDTO {

    public enum Severity { INFO, WARNING, ERROR }

    private Severity severity;
    private String code;
    private String message;

    public AlertDTO() {}

    public AlertDTO(Severity severity, String code, String message) {
        this.severity = severity;
        this.code = code;
        this.message = message;
    }

    public Severity getSeverity() { return severity; }
    public void setSeverity(Severity severity) { this.severity = severity; }
    public String getCode() { return code; }
    public void setCode(String code) { this.code = code; }
    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }
}

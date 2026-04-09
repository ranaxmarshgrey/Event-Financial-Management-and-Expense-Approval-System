package com.ooad.efms.strategy;

public class ApprovalDecision {

    public enum Outcome { AUTO_APPROVED, REQUIRES_MANUAL_REVIEW, REJECTED }

    private final Outcome outcome;
    private final String reason;
    private final String strategyName;

    public ApprovalDecision(Outcome outcome, String reason, String strategyName) {
        this.outcome = outcome;
        this.reason = reason;
        this.strategyName = strategyName;
    }

    public Outcome getOutcome() { return outcome; }
    public String getReason() { return reason; }
    public String getStrategyName() { return strategyName; }
}

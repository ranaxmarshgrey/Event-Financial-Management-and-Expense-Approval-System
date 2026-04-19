package com.ooad.efms.service.rules;

/**
 * Chain of Responsibility — single link.
 *
 * Each rule inspects the {@link ApprovalContext} and either passes silently
 * or throws {@link com.ooad.efms.exception.RuleViolationException} to stop
 * the chain. ExpenseApprovalService injects all {@code @Component} rules as
 * a {@code List<ApprovalRule>} (Spring wires them by {@code @Order}), so
 * adding a new rule is a new class — no edits to the service (Open/Closed).
 */
public interface ApprovalRule {
    void check(ApprovalContext ctx);
}

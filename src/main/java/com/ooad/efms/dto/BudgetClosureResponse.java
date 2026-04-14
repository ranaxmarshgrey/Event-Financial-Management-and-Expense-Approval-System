package com.ooad.efms.dto;

import com.ooad.efms.model.Budget;
import com.ooad.efms.model.Expense;
import com.ooad.efms.model.ExpenseStatus;

import java.math.BigDecimal;
import java.util.List;

/**
 * UC #4 — Final Budget Closure financial summary.
 *
 * Snapshot of the budget at the moment of closure: totals, per-category
 * breakdown, and expense-count stats. Returned by POST /api/budgets/{id}/close.
 */
public class BudgetClosureResponse {

    private Long budgetId;
    private String eventName;
    private String status;
    private BigDecimal totalBudget;
    private BigDecimal totalAllocated;
    private BigDecimal totalSpent;
    private BigDecimal remaining;
    private int approvedExpenseCount;
    private int rejectedExpenseCount;
    private List<CategorySummary> categories;

    public static BudgetClosureResponse from(Budget budget, List<Expense> expenses) {
        BudgetClosureResponse r = new BudgetClosureResponse();
        r.budgetId = budget.getId();
        r.eventName = budget.getEvent().getName();
        r.status = budget.getStatus().name();
        r.totalBudget = budget.getTotalLimit();
        r.totalAllocated = budget.getAllocatedTotal();
        r.totalSpent = budget.getCategories().stream()
                .map(c -> c.getSpentAmount() == null ? BigDecimal.ZERO : c.getSpentAmount())
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        r.remaining = budget.getTotalLimit().subtract(r.totalSpent);
        r.approvedExpenseCount = (int) expenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.APPROVED).count();
        r.rejectedExpenseCount = (int) expenses.stream()
                .filter(e -> e.getStatus() == ExpenseStatus.REJECTED).count();
        r.categories = budget.getCategories().stream()
                .map(c -> new CategorySummary(
                        c.getId(),
                        c.getName(),
                        c.getAllocatedAmount(),
                        c.getSpentAmount() == null ? BigDecimal.ZERO : c.getSpentAmount()))
                .toList();
        return r;
    }

    public Long getBudgetId() { return budgetId; }
    public String getEventName() { return eventName; }
    public String getStatus() { return status; }
    public BigDecimal getTotalBudget() { return totalBudget; }
    public BigDecimal getTotalAllocated() { return totalAllocated; }
    public BigDecimal getTotalSpent() { return totalSpent; }
    public BigDecimal getRemaining() { return remaining; }
    public int getApprovedExpenseCount() { return approvedExpenseCount; }
    public int getRejectedExpenseCount() { return rejectedExpenseCount; }
    public List<CategorySummary> getCategories() { return categories; }

    public static class CategorySummary {
        private final Long id;
        private final String name;
        private final BigDecimal allocated;
        private final BigDecimal spent;
        private final BigDecimal variance;

        public CategorySummary(Long id, String name, BigDecimal allocated, BigDecimal spent) {
            this.id = id;
            this.name = name;
            this.allocated = allocated;
            this.spent = spent;
            this.variance = allocated.subtract(spent);
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public BigDecimal getAllocated() { return allocated; }
        public BigDecimal getSpent() { return spent; }
        public BigDecimal getVariance() { return variance; }
    }
}

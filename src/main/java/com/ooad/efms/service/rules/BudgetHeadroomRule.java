package com.ooad.efms.service.rules;

import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.ExpenseCategory;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * Chain link #6 — prevents approval from pushing the category's approved
 * total over its allocation. Uses the stored spentAmount (which represents
 * approved expenses only) so pending/rejected claims don't affect headroom.
 */
@Component
@Order(60)
public class BudgetHeadroomRule implements ApprovalRule {

    @Override
    public void check(ApprovalContext ctx) {
        ExpenseCategory category = ctx.getExpense().getCategory();
        BigDecimal projected = category.getSpentAmount().add(ctx.getExpense().getAmount());
        if (projected.compareTo(category.getAllocatedAmount()) > 0) {
            throw new RuleViolationException(
                    "INSUFFICIENT_CATEGORY_HEADROOM",
                    "Approving this expense would overspend category '" + category.getName()
                            + "'. Approved so far=" + category.getSpentAmount()
                            + ", this expense=" + ctx.getExpense().getAmount()
                            + ", allocated=" + category.getAllocatedAmount());
        }
    }
}

package com.ooad.efms.service;

import com.ooad.efms.dto.AlertDTO;
import com.ooad.efms.model.Budget;
import com.ooad.efms.model.ExpenseCategory;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

/**
 * Minor use case: Budget Variance Alerts.
 *
 * Single Responsibility — this service only produces alerts; it does not
 * mutate the budget or decide whether to block submission. BudgetService
 * owns the decision of what to do with the alerts.
 */
@Service
public class VarianceAlertService {

    /** Warning fires when allocation reaches 90% of the total limit. */
    private static final BigDecimal WARNING_RATIO = new BigDecimal("0.90");

    public List<AlertDTO> check(Budget budget) {
        List<AlertDTO> alerts = new ArrayList<>();

        BigDecimal allocated = budget.getAllocatedTotal();
        BigDecimal limit = budget.getTotalLimit();

        if (budget.getCategories().isEmpty()) {
            alerts.add(new AlertDTO(
                    AlertDTO.Severity.ERROR,
                    "NO_CATEGORIES",
                    "Budget has no expense categories defined"));
        }

        if (allocated.compareTo(limit) > 0) {
            alerts.add(new AlertDTO(
                    AlertDTO.Severity.ERROR,
                    "ALLOCATION_EXCEEDS_LIMIT",
                    "Total allocation (" + allocated + ") exceeds budget limit (" + limit + ")"));
        } else if (allocated.compareTo(limit.multiply(WARNING_RATIO)) >= 0) {
            alerts.add(new AlertDTO(
                    AlertDTO.Severity.WARNING,
                    "ALLOCATION_NEAR_LIMIT",
                    "Total allocation is at/near 90% of the budget limit"));
        }

        for (ExpenseCategory c : budget.getCategories()) {
            if (c.getAllocatedAmount().compareTo(BigDecimal.ZERO) <= 0) {
                alerts.add(new AlertDTO(
                        AlertDTO.Severity.ERROR,
                        "INVALID_CATEGORY_ALLOCATION",
                        "Category '" + c.getName() + "' has non-positive allocation"));
            }
            if (c.getSpentAmount().compareTo(c.getAllocatedAmount()) > 0) {
                alerts.add(new AlertDTO(
                        AlertDTO.Severity.ERROR,
                        "CATEGORY_OVERSPENT",
                        "Category '" + c.getName() + "' has overspent its allocation"));
            }
        }

        return alerts;
    }

    public boolean hasBlockingAlerts(List<AlertDTO> alerts) {
        return alerts.stream().anyMatch(a -> a.getSeverity() == AlertDTO.Severity.ERROR);
    }
}

package com.ooad.efms.service;

import com.ooad.efms.dto.ExpenseResponse;
import com.ooad.efms.dto.SubmitExpenseRequest;
import com.ooad.efms.exception.InvalidBudgetException;
import com.ooad.efms.exception.ResourceNotFoundException;
import com.ooad.efms.exception.RuleViolationException;
import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.Budget;
import com.ooad.efms.model.BudgetStatus;
import com.ooad.efms.model.CategoryRule;
import com.ooad.efms.model.Expense;
import com.ooad.efms.model.ExpenseCategory;
import com.ooad.efms.model.ExpenseStatus;
import com.ooad.efms.model.Organizer;
import com.ooad.efms.repository.ExpenseRepository;
import com.ooad.efms.repository.OrganizerRepository;
import com.ooad.efms.strategy.ApprovalRoutingStrategy;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * Major use case: Submit Expense Claim.
 *
 * Workflow:
 *   1. Organizer submits an expense against a category of an APPROVED budget.
 *   2. Service validates the claim (budget state, category headroom, amount).
 *   3. ApprovalRoutingStrategy (minor UC: Role-Based Approval Flow) stamps
 *      the expense with its required approval chain.
 *   4. Expense is persisted as PENDING_APPROVAL — actual approve/reject
 *      belongs to use case #3.
 *
 * Design notes:
 *  - SRP: validation + persistence only; routing policy is external.
 *  - DIP: depends on {@link ApprovalRoutingStrategy} interface.
 *  - OCP: new routing policies added as new strategies, no edits here.
 */
@Service
@Transactional
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final OrganizerRepository organizerRepository;
    private final ApprovalRoutingStrategy routingStrategy;
    private final EntityManager entityManager;

    public ExpenseService(ExpenseRepository expenseRepository,
                          OrganizerRepository organizerRepository,
                          ApprovalRoutingStrategy routingStrategy,
                          EntityManager entityManager) {
        this.expenseRepository = expenseRepository;
        this.organizerRepository = organizerRepository;
        this.routingStrategy = routingStrategy;
        this.entityManager = entityManager;
    }

    public ExpenseResponse submit(SubmitExpenseRequest req) {
        ExpenseCategory category = entityManager.find(ExpenseCategory.class, req.getCategoryId());
        if (category == null) {
            throw new ResourceNotFoundException("Expense category not found: " + req.getCategoryId());
        }
        Budget budget = category.getBudget();
        if (budget.getStatus() != BudgetStatus.APPROVED) {
            throw new InvalidBudgetException(
                    "Expenses can only be submitted against an APPROVED budget (current: " + budget.getStatus() + ")");
        }

        Organizer organizer = organizerRepository.findById(req.getSubmittedById())
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found: " + req.getSubmittedById()));

        // Minor UC #3 — category rules enforced at submission time.
        CategoryRule rule = category.getRule();
        if (rule != null) {
            if (rule.isBlocked()) {
                throw new RuleViolationException(
                        "CATEGORY_BLOCKED",
                        "Category '" + category.getName() + "' is blocked for new expenses");
            }
            if (rule.getMaxExpenseAmount() != null
                    && req.getAmount().compareTo(rule.getMaxExpenseAmount()) > 0) {
                throw new RuleViolationException(
                        "EXCEEDS_CATEGORY_MAX",
                        "Expense amount " + req.getAmount()
                                + " exceeds category cap of " + rule.getMaxExpenseAmount());
            }
        }

        BigDecimal committed = expenseRepository.sumCommittedByCategory(category.getId(), ExpenseStatus.REJECTED);
        BigDecimal projected = committed.add(req.getAmount());
        if (projected.compareTo(category.getAllocatedAmount()) > 0) {
            throw new InvalidBudgetException(
                    "Expense would exceed category allocation. Allocated=" + category.getAllocatedAmount()
                            + ", already committed=" + committed + ", requested=" + req.getAmount());
        }

        Expense expense = new Expense(
                req.getDescription(),
                req.getAmount(),
                req.getExpenseDate(),
                req.getSupportingDocUrl(),
                category,
                organizer,
                ApprovalLevel.LEVEL_1);
        ApprovalLevel level = routingStrategy.routeFor(expense);
        // Category rule can upgrade the required chain (never downgrade).
        if (rule != null
                && rule.getRequiresL2ApprovalAbove() != null
                && req.getAmount().compareTo(rule.getRequiresL2ApprovalAbove()) > 0) {
            level = ApprovalLevel.LEVEL_1_AND_2;
        }
        expense.setRequiredApprovalLevel(level);

        expense = expenseRepository.save(expense);
        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listByBudget(Long budgetId) {
        return expenseRepository.findByCategoryBudgetId(budgetId).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<ExpenseResponse> listPending() {
        return expenseRepository.findByStatusOrderBySubmittedAtAsc(ExpenseStatus.PENDING_APPROVAL).stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public ExpenseResponse get(Long id) {
        Expense e = expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
        return ExpenseResponse.from(e);
    }

    @Transactional(readOnly = true)
    public String activeRoutingPolicy() {
        return routingStrategy.name();
    }
}

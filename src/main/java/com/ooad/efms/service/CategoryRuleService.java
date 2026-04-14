package com.ooad.efms.service;

import com.ooad.efms.dto.CategoryRuleResponse;
import com.ooad.efms.dto.SetCategoryRuleRequest;
import com.ooad.efms.exception.ResourceNotFoundException;
import com.ooad.efms.model.CategoryRule;
import com.ooad.efms.model.ExpenseCategory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Minor use case #3 — Expense Category Rules.
 *
 * Finance-admin facing. Upserts the per-category rule that governs:
 *   - maxExpenseAmount       (hard cap per single claim)
 *   - requiresL2ApprovalAbove (threshold forcing LEVEL_1_AND_2)
 *   - blocked                (short-circuit flag stopping new approvals)
 */
@Service
@Transactional
public class CategoryRuleService {

    private final EntityManager entityManager;

    public CategoryRuleService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public CategoryRuleResponse upsert(Long categoryId, SetCategoryRuleRequest req) {
        ExpenseCategory category = entityManager.find(ExpenseCategory.class, categoryId);
        if (category == null) {
            throw new ResourceNotFoundException("Expense category not found: " + categoryId);
        }
        CategoryRule rule = category.getRule();
        if (rule == null) {
            rule = new CategoryRule(category);
            category.setRule(rule);
        }
        if (req.getMaxExpenseAmount() != null)       rule.setMaxExpenseAmount(req.getMaxExpenseAmount());
        if (req.getRequiresL2ApprovalAbove() != null) rule.setRequiresL2ApprovalAbove(req.getRequiresL2ApprovalAbove());
        if (req.getBlocked() != null)                 rule.setBlocked(req.getBlocked());
        entityManager.persist(rule);
        entityManager.flush();
        return CategoryRuleResponse.from(rule);
    }

    @Transactional(readOnly = true)
    public CategoryRuleResponse get(Long categoryId) {
        ExpenseCategory category = entityManager.find(ExpenseCategory.class, categoryId);
        if (category == null) {
            throw new ResourceNotFoundException("Expense category not found: " + categoryId);
        }
        CategoryRule rule = category.getRule();
        if (rule == null) {
            // Return an empty rule shape so the UI can render defaults.
            CategoryRule empty = new CategoryRule(category);
            return CategoryRuleResponse.from(empty);
        }
        return CategoryRuleResponse.from(rule);
    }
}

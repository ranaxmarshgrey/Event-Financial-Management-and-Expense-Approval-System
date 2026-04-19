package com.ooad.efms.repository;

import com.ooad.efms.model.Expense;
import com.ooad.efms.model.ExpenseStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface ExpenseRepository extends JpaRepository<Expense, Long> {

    List<Expense> findByCategoryId(Long categoryId);

    List<Expense> findByCategoryBudgetId(Long budgetId);

    List<Expense> findByStatusOrderBySubmittedAtAsc(ExpenseStatus status);

    /**
     * Sum of expenses against a category that are not yet rejected — used to
     * enforce category headroom at submission time so pending claims still
     * count against the allocation.
     */
    @org.springframework.data.jpa.repository.Query(
        "select coalesce(sum(e.amount), 0) from Expense e " +
        "where e.category.id = :categoryId and e.status <> :excluded")
    BigDecimal sumCommittedByCategory(
        @org.springframework.data.repository.query.Param("categoryId") Long categoryId,
        @org.springframework.data.repository.query.Param("excluded") ExpenseStatus excluded);

    Optional<Expense> findFirstByCategoryBudgetIdOrderBySubmittedAtDesc(Long budgetId);
}

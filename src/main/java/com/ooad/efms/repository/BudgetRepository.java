package com.ooad.efms.repository;

import com.ooad.efms.model.Budget;
import com.ooad.efms.model.BudgetStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BudgetRepository extends JpaRepository<Budget, Long> {
    List<Budget> findByStatus(BudgetStatus status);
}

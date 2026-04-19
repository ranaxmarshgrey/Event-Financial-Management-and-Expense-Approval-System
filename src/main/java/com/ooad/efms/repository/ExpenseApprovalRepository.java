package com.ooad.efms.repository;

import com.ooad.efms.model.ExpenseApproval;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface ExpenseApprovalRepository extends JpaRepository<ExpenseApproval, Long> {
    List<ExpenseApproval> findByExpenseIdOrderByActedAtAsc(Long expenseId);
}

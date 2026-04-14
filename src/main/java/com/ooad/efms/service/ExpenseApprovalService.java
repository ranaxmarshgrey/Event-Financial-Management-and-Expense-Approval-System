package com.ooad.efms.service;

import com.ooad.efms.dto.ApproveExpenseRequest;
import com.ooad.efms.dto.ExpenseApprovalResponse;
import com.ooad.efms.dto.ExpenseResponse;
import com.ooad.efms.dto.RejectExpenseRequest;
import com.ooad.efms.exception.InvalidBudgetException;
import com.ooad.efms.exception.ResourceNotFoundException;
import com.ooad.efms.model.ApprovalAction;
import com.ooad.efms.model.ApprovalLevel;
import com.ooad.efms.model.ApprovalRole;
import com.ooad.efms.model.ApprovingAuthority;
import com.ooad.efms.model.Expense;
import com.ooad.efms.model.ExpenseApproval;
import com.ooad.efms.model.ExpenseCategory;
import com.ooad.efms.model.ExpenseStatus;
import com.ooad.efms.repository.ApprovingAuthorityRepository;
import com.ooad.efms.repository.ExpenseApprovalRepository;
import com.ooad.efms.repository.ExpenseRepository;
import com.ooad.efms.service.rules.ApprovalContext;
import com.ooad.efms.service.rules.ApprovalRule;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Major use case #3 — Approve or Reject Expense.
 *
 * Applies a Chain-of-Responsibility rule pipeline (budget availability,
 * category rules, approver level, approval limits) before recording the
 * action. LEVEL_1_AND_2 expenses need two successive APPROVE records before
 * transitioning to APPROVED; on final approval, the category's spentAmount
 * is incremented so downstream checks (headroom, future UC #4 closure) see
 * the updated state.
 *
 * Design notes:
 *  - SRP: all rule enforcement is pushed into @Component rules; this
 *          service only orchestrates and persists.
 *  - DIP: depends on {@link ApprovalRule} interface (list), not concrete rules.
 *  - OCP: adding a new rule = new @Component class, no edits here.
 */
@Service
@Transactional
public class ExpenseApprovalService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseApprovalRepository approvalRepository;
    private final ApprovingAuthorityRepository authorityRepository;
    private final List<ApprovalRule> ruleChain;

    public ExpenseApprovalService(ExpenseRepository expenseRepository,
                                  ExpenseApprovalRepository approvalRepository,
                                  ApprovingAuthorityRepository authorityRepository,
                                  List<ApprovalRule> ruleChain) {
        this.expenseRepository = expenseRepository;
        this.approvalRepository = approvalRepository;
        this.authorityRepository = authorityRepository;
        this.ruleChain = ruleChain;
    }

    public ExpenseResponse approve(Long expenseId, ApproveExpenseRequest req) {
        Expense expense = loadExpense(expenseId);
        ApprovingAuthority approver = loadApprover(req.getApproverId());
        List<ExpenseApproval> prior = approvalRepository.findByExpenseIdOrderByActedAtAsc(expenseId);

        // Chain of Responsibility — every rule either passes or throws.
        ApprovalContext ctx = new ApprovalContext(expense, approver, prior);
        ruleChain.forEach(rule -> rule.check(ctx));

        ExpenseApproval record = new ExpenseApproval(expense, approver, ApprovalAction.APPROVE, req.getNotes());
        approvalRepository.save(record);

        if (isChainComplete(expense, prior, approver.getRole())) {
            expense.setStatus(ExpenseStatus.APPROVED);
            ExpenseCategory category = expense.getCategory();
            category.setSpentAmount(category.getSpentAmount().add(expense.getAmount()));
        }
        return ExpenseResponse.from(expense);
    }

    public ExpenseResponse reject(Long expenseId, RejectExpenseRequest req) {
        Expense expense = loadExpense(expenseId);
        if (expense.getStatus() != ExpenseStatus.PENDING_APPROVAL) {
            throw new InvalidBudgetException(
                    "Expense is already " + expense.getStatus() + " and cannot be re-actioned");
        }
        ApprovingAuthority approver = loadApprover(req.getApproverId());
        ExpenseApproval record = new ExpenseApproval(expense, approver, ApprovalAction.REJECT, req.getReason());
        approvalRepository.save(record);
        expense.setStatus(ExpenseStatus.REJECTED);
        return ExpenseResponse.from(expense);
    }

    @Transactional(readOnly = true)
    public List<ExpenseApprovalResponse> history(Long expenseId) {
        if (!expenseRepository.existsById(expenseId)) {
            throw new ResourceNotFoundException("Expense not found: " + expenseId);
        }
        return approvalRepository.findByExpenseIdOrderByActedAtAsc(expenseId).stream()
                .map(ExpenseApprovalResponse::from)
                .toList();
    }

    private boolean isChainComplete(Expense expense, List<ExpenseApproval> prior, ApprovalRole justActedRole) {
        if (expense.getRequiredApprovalLevel() == ApprovalLevel.LEVEL_1) {
            return justActedRole == ApprovalRole.L1;
        }
        // LEVEL_1_AND_2: complete when L2 approves AND prior list contains an L1 APPROVE.
        boolean l1Done = prior.stream()
                .anyMatch(a -> a.getApproverRoleAtAction() == ApprovalRole.L1
                        && a.getAction() == ApprovalAction.APPROVE);
        return justActedRole == ApprovalRole.L2 && l1Done;
    }

    private Expense loadExpense(Long id) {
        return expenseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Expense not found: " + id));
    }

    private ApprovingAuthority loadApprover(Long id) {
        return authorityRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Approving authority not found: " + id));
    }
}

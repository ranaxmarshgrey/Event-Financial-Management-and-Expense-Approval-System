package com.ooad.efms.controller;

import com.ooad.efms.dto.ApproveExpenseRequest;
import com.ooad.efms.dto.ApprovingAuthorityResponse;
import com.ooad.efms.dto.ExpenseApprovalResponse;
import com.ooad.efms.dto.ExpenseResponse;
import com.ooad.efms.dto.RejectExpenseRequest;
import com.ooad.efms.repository.ApprovingAuthorityRepository;
import com.ooad.efms.service.ExpenseApprovalService;
import com.ooad.efms.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * REST endpoints for the Approving Authority role (major UC #3).
 */
@RestController
public class ApprovalController {

    private final ExpenseApprovalService approvalService;
    private final ExpenseService expenseService;
    private final ApprovingAuthorityRepository authorityRepository;

    public ApprovalController(ExpenseApprovalService approvalService,
                              ExpenseService expenseService,
                              ApprovingAuthorityRepository authorityRepository) {
        this.approvalService = approvalService;
        this.expenseService = expenseService;
        this.authorityRepository = authorityRepository;
    }

    @PostMapping("/api/expenses/{id}/approve")
    public ResponseEntity<ExpenseResponse> approve(@PathVariable Long id,
                                                   @Valid @RequestBody ApproveExpenseRequest req) {
        return ResponseEntity.ok(approvalService.approve(id, req));
    }

    @PostMapping("/api/expenses/{id}/reject")
    public ResponseEntity<ExpenseResponse> reject(@PathVariable Long id,
                                                  @Valid @RequestBody RejectExpenseRequest req) {
        return ResponseEntity.ok(approvalService.reject(id, req));
    }

    @GetMapping("/api/expenses/{id}/history")
    public ResponseEntity<List<ExpenseApprovalResponse>> history(@PathVariable Long id) {
        return ResponseEntity.ok(approvalService.history(id));
    }

    @GetMapping("/api/expenses/pending")
    public ResponseEntity<List<ExpenseResponse>> pending() {
        return ResponseEntity.ok(expenseService.listPending());
    }

    @GetMapping("/api/approving-authorities")
    public ResponseEntity<List<ApprovingAuthorityResponse>> listApprovers() {
        return ResponseEntity.ok(authorityRepository.findAll().stream()
                .map(ApprovingAuthorityResponse::from)
                .toList());
    }
}

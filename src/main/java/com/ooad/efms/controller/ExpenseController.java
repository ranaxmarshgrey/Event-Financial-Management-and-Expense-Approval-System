package com.ooad.efms.controller;

import com.ooad.efms.dto.ExpenseResponse;
import com.ooad.efms.dto.SubmitExpenseRequest;
import com.ooad.efms.service.ExpenseService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/expenses")
public class ExpenseController {

    private final ExpenseService expenseService;

    public ExpenseController(ExpenseService expenseService) {
        this.expenseService = expenseService;
    }

    @PostMapping
    public ResponseEntity<ExpenseResponse> submit(@Valid @RequestBody SubmitExpenseRequest req) {
        return ResponseEntity.ok(expenseService.submit(req));
    }

    @GetMapping
    public ResponseEntity<List<ExpenseResponse>> listByBudget(@RequestParam Long budgetId) {
        return ResponseEntity.ok(expenseService.listByBudget(budgetId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ExpenseResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(expenseService.get(id));
    }

    /** Diagnostic: shows which ApprovalRoutingStrategy is currently active. */
    @GetMapping("/routing-policy")
    public ResponseEntity<Map<String, String>> routingPolicy() {
        return ResponseEntity.ok(Map.of("activeStrategy", expenseService.activeRoutingPolicy()));
    }
}

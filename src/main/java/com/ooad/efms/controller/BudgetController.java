package com.ooad.efms.controller;

import com.ooad.efms.dto.AddCategoryRequest;
import com.ooad.efms.dto.AlertDTO;
import com.ooad.efms.dto.BudgetResponse;
import com.ooad.efms.dto.CreateEventRequest;
import com.ooad.efms.service.BudgetService;
import com.ooad.efms.strategy.ApprovalDecision;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/budgets")
public class BudgetController {

    private final BudgetService budgetService;

    public BudgetController(BudgetService budgetService) {
        this.budgetService = budgetService;
    }

    @PostMapping
    public ResponseEntity<BudgetResponse> create(@Valid @RequestBody CreateEventRequest req) {
        return ResponseEntity.ok(budgetService.createEventWithDraftBudget(req));
    }

    @PostMapping("/{id}/categories")
    public ResponseEntity<BudgetResponse> addCategory(@PathVariable Long id,
                                                      @Valid @RequestBody AddCategoryRequest req) {
        return ResponseEntity.ok(budgetService.addCategory(id, req));
    }

    @PostMapping("/{id}/validate")
    public ResponseEntity<List<AlertDTO>> validate(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.validate(id));
    }

    @PostMapping("/{id}/submit")
    public ResponseEntity<ApprovalDecision> submit(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.submit(id));
    }

    @GetMapping("/{id}")
    public ResponseEntity<BudgetResponse> get(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.get(id));
    }
}

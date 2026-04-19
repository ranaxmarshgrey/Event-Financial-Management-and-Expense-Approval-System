package com.ooad.efms.controller;

import com.ooad.efms.dto.CategoryRuleResponse;
import com.ooad.efms.dto.SetCategoryRuleRequest;
import com.ooad.efms.service.CategoryRuleService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST endpoints for minor UC #3 — Expense Category Rules.
 * Finance-admin facing: configure per-category caps, thresholds, and blocks.
 */
@RestController
@RequestMapping("/api/categories/{categoryId}/rule")
public class CategoryRuleController {

    private final CategoryRuleService ruleService;

    public CategoryRuleController(CategoryRuleService ruleService) {
        this.ruleService = ruleService;
    }

    @GetMapping
    public ResponseEntity<CategoryRuleResponse> get(@PathVariable Long categoryId) {
        return ResponseEntity.ok(ruleService.get(categoryId));
    }

    @PutMapping
    public ResponseEntity<CategoryRuleResponse> upsert(@PathVariable Long categoryId,
                                                       @Valid @RequestBody SetCategoryRuleRequest req) {
        return ResponseEntity.ok(ruleService.upsert(categoryId, req));
    }
}

package com.ooad.efms.controller;

import com.ooad.efms.dto.AddCategoryRequest;
import com.ooad.efms.dto.AlertDTO;
import com.ooad.efms.dto.BudgetClosureResponse;
import com.ooad.efms.dto.BudgetResponse;
import com.ooad.efms.dto.CreateEventRequest;
import com.ooad.efms.service.BudgetService;
import com.ooad.efms.strategy.ApprovalDecision;
import jakarta.validation.Valid;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
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

    @GetMapping
    public ResponseEntity<List<BudgetResponse>> listAll() {
        return ResponseEntity.ok(budgetService.listAll());
    }

    @GetMapping("/pending-approval")
    public ResponseEntity<List<BudgetResponse>> pendingApproval() {
        return ResponseEntity.ok(budgetService.listPendingApproval());
    }

    @PostMapping("/{id}/manual-approve")
    public ResponseEntity<BudgetResponse> manualApprove(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.manualApprove(id));
    }

    @PostMapping("/{id}/manual-reject")
    public ResponseEntity<BudgetResponse> manualReject(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.manualReject(id));
    }

    @PostMapping("/{id}/close")
    public ResponseEntity<BudgetClosureResponse> close(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.closeBudget(id));
    }

    @GetMapping("/{id}/closure-summary")
    public ResponseEntity<BudgetClosureResponse> closureSummary(@PathVariable Long id) {
        return ResponseEntity.ok(budgetService.getClosureSummary(id));
    }

    @GetMapping(value = "/{id}/closure-summary.csv", produces = "text/csv")
    public ResponseEntity<String> closureSummaryCsv(@PathVariable Long id) {
        BudgetClosureResponse s = budgetService.getClosureSummary(id);
        StringBuilder csv = new StringBuilder();
        csv.append("Event,").append(escape(s.getEventName())).append('\n');
        csv.append("Budget ID,").append(s.getBudgetId()).append('\n');
        csv.append("Status,").append(s.getStatus()).append('\n');
        csv.append("Total Budget,").append(s.getTotalBudget()).append('\n');
        csv.append("Total Allocated,").append(s.getTotalAllocated()).append('\n');
        csv.append("Total Spent,").append(s.getTotalSpent()).append('\n');
        csv.append("Remaining,").append(s.getRemaining()).append('\n');
        csv.append("Approved Claims,").append(s.getApprovedExpenseCount()).append('\n');
        csv.append("Rejected Claims,").append(s.getRejectedExpenseCount()).append('\n');
        csv.append('\n');
        csv.append("Category,Allocated,Spent,Variance\n");
        s.getCategories().forEach(c -> csv.append(escape(c.getName())).append(',')
                .append(c.getAllocated()).append(',')
                .append(c.getSpent()).append(',')
                .append(c.getVariance()).append('\n'));
        String filename = "budget-" + id + "-closure-summary.csv";
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType("text/csv"))
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + filename + "\"")
                .body(csv.toString());
    }

    /** RFC 4180 minimal escaping: wrap in quotes if value contains comma, quote, or newline. */
    private static String escape(String v) {
        if (v == null) return "";
        if (v.contains(",") || v.contains("\"") || v.contains("\n")) {
            return "\"" + v.replace("\"", "\"\"") + "\"";
        }
        return v;
    }
}

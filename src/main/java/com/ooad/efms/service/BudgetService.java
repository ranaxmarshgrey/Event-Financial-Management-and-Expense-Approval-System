package com.ooad.efms.service;

import com.ooad.efms.dto.AddCategoryRequest;
import com.ooad.efms.dto.AlertDTO;
import com.ooad.efms.dto.BudgetResponse;
import com.ooad.efms.dto.CreateEventRequest;
import com.ooad.efms.exception.InvalidBudgetException;
import com.ooad.efms.exception.ResourceNotFoundException;
import com.ooad.efms.model.Budget;
import com.ooad.efms.model.BudgetStatus;
import com.ooad.efms.model.Event;
import com.ooad.efms.model.ExpenseCategory;
import com.ooad.efms.model.Organizer;
import com.ooad.efms.repository.BudgetRepository;
import com.ooad.efms.repository.EventRepository;
import com.ooad.efms.repository.OrganizerRepository;
import com.ooad.efms.strategy.ApprovalDecision;
import com.ooad.efms.strategy.ApprovalStrategy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * Major use case: Create Event Budget.
 *
 * Orchestrates the workflow:
 *   createEventWithDraftBudget -> addCategory (0..n) -> validate -> submit
 *
 * Design notes:
 *  - SRP: business logic lives here, not in the controller (which only handles HTTP)
 *          and not in the entities (which only hold state).
 *  - DIP: depends on ApprovalStrategy and VarianceAlertService abstractions,
 *          not on their concrete implementations.
 *  - OCP: new approval policies can be added without touching this class.
 */
@Service
@Transactional
public class BudgetService {

    private final EventRepository eventRepository;
    private final BudgetRepository budgetRepository;
    private final OrganizerRepository organizerRepository;
    private final VarianceAlertService varianceAlertService;
    private final ApprovalStrategy approvalStrategy;

    public BudgetService(EventRepository eventRepository,
                         BudgetRepository budgetRepository,
                         OrganizerRepository organizerRepository,
                         VarianceAlertService varianceAlertService,
                         ApprovalStrategy approvalStrategy) {
        this.eventRepository = eventRepository;
        this.budgetRepository = budgetRepository;
        this.organizerRepository = organizerRepository;
        this.varianceAlertService = varianceAlertService;
        this.approvalStrategy = approvalStrategy;
    }

    /** Step 1: Organizer creates a new event and its empty draft budget. */
    public BudgetResponse createEventWithDraftBudget(CreateEventRequest req) {
        if (req.getEndDate().isBefore(req.getStartDate())) {
            throw new InvalidBudgetException("Event end date must be on or after start date");
        }
        Organizer organizer = organizerRepository.findById(req.getOrganizerId())
                .orElseThrow(() -> new ResourceNotFoundException("Organizer not found: " + req.getOrganizerId()));

        Event event = new Event(
                req.getName(),
                req.getDescription(),
                req.getStartDate(),
                req.getEndDate(),
                organizer);
        Budget budget = new Budget(req.getTotalBudget(), event);
        event.setBudget(budget);
        eventRepository.save(event);
        return BudgetResponse.from(budget);
    }

    /** Step 2: Organizer defines expense categories and allocates funds. Only allowed in DRAFT. */
    public BudgetResponse addCategory(Long budgetId, AddCategoryRequest req) {
        Budget budget = loadBudget(budgetId);
        requireStatus(budget, BudgetStatus.DRAFT);
        ExpenseCategory category = new ExpenseCategory(req.getName(), req.getAllocatedAmount());
        budget.addCategory(category);
        return BudgetResponse.from(budget);
    }

    /** Step 3: Run variance checks. If no blocking alerts, move DRAFT -> READY. */
    public List<AlertDTO> validate(Long budgetId) {
        Budget budget = loadBudget(budgetId);
        List<AlertDTO> alerts = varianceAlertService.check(budget);
        if (!varianceAlertService.hasBlockingAlerts(alerts) && budget.getStatus() == BudgetStatus.DRAFT) {
            budget.setStatus(BudgetStatus.READY);
        }
        return alerts;
    }

    /** Step 4: Submit for approval. Strategy decides the outcome. */
    public ApprovalDecision submit(Long budgetId) {
        Budget budget = loadBudget(budgetId);
        List<AlertDTO> alerts = varianceAlertService.check(budget);
        if (varianceAlertService.hasBlockingAlerts(alerts)) {
            throw new InvalidBudgetException("Cannot submit budget with blocking variance alerts; run /validate first");
        }
        if (budget.getStatus() != BudgetStatus.READY && budget.getStatus() != BudgetStatus.DRAFT) {
            throw new InvalidBudgetException("Budget cannot be submitted from status " + budget.getStatus());
        }
        budget.setStatus(BudgetStatus.SUBMITTED);
        ApprovalDecision decision = approvalStrategy.evaluate(budget);
        switch (decision.getOutcome()) {
            case AUTO_APPROVED -> budget.setStatus(BudgetStatus.APPROVED);
            case REJECTED -> budget.setStatus(BudgetStatus.REJECTED);
            case REQUIRES_MANUAL_REVIEW -> { /* remains SUBMITTED pending human review */ }
        }
        return decision;
    }

    public BudgetResponse get(Long budgetId) {
        return BudgetResponse.from(loadBudget(budgetId));
    }

    private Budget loadBudget(Long id) {
        return budgetRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Budget not found: " + id));
    }

    private void requireStatus(Budget budget, BudgetStatus expected) {
        if (budget.getStatus() != expected) {
            throw new InvalidBudgetException(
                    "Operation requires budget status " + expected + " but was " + budget.getStatus());
        }
    }
}

package com.ooad.efms.dto;

import com.ooad.efms.model.Budget;
import com.ooad.efms.model.BudgetStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

public class BudgetResponse {

    private Long id;
    private Long eventId;
    private String eventName;
    private Long organizerId;
    private BigDecimal totalLimit;
    private BigDecimal allocatedTotal;
    private BigDecimal remaining;
    private BudgetStatus status;
    private List<CategoryResponse> categories;

    public static BudgetResponse from(Budget b) {
        BudgetResponse r = new BudgetResponse();
        r.id = b.getId();
        r.eventId = b.getEvent().getId();
        r.eventName = b.getEvent().getName();
        r.organizerId = b.getEvent().getOrganizer() != null ? b.getEvent().getOrganizer().getId() : null;
        r.totalLimit = b.getTotalLimit();
        r.allocatedTotal = b.getAllocatedTotal();
        r.remaining = b.getTotalLimit().subtract(b.getAllocatedTotal());
        r.status = b.getStatus();
        r.categories = b.getCategories().stream()
                .map(CategoryResponse::from)
                .collect(Collectors.toList());
        return r;
    }

    public Long getId() { return id; }
    public Long getEventId() { return eventId; }
    public String getEventName() { return eventName; }
    public Long getOrganizerId() { return organizerId; }
    public BigDecimal getTotalLimit() { return totalLimit; }
    public BigDecimal getAllocatedTotal() { return allocatedTotal; }
    public BigDecimal getRemaining() { return remaining; }
    public BudgetStatus getStatus() { return status; }
    public List<CategoryResponse> getCategories() { return categories; }
}

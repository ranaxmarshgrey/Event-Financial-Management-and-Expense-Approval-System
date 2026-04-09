package com.ooad.efms.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "budgets")
public class Budget {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal totalLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private BudgetStatus status = BudgetStatus.DRAFT;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "event_id", nullable = false, unique = true)
    @JsonIgnore
    private Event event;

    @OneToMany(mappedBy = "budget", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ExpenseCategory> categories = new ArrayList<>();

    public Budget() {}

    public Budget(BigDecimal totalLimit, Event event) {
        this.totalLimit = totalLimit;
        this.event = event;
    }

    /** Sum of allocated amounts across all categories. */
    public BigDecimal getAllocatedTotal() {
        return categories.stream()
                .map(ExpenseCategory::getAllocatedAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public void addCategory(ExpenseCategory category) {
        categories.add(category);
        category.setBudget(this);
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public BigDecimal getTotalLimit() { return totalLimit; }
    public void setTotalLimit(BigDecimal totalLimit) { this.totalLimit = totalLimit; }
    public BudgetStatus getStatus() { return status; }
    public void setStatus(BudgetStatus status) { this.status = status; }
    public Event getEvent() { return event; }
    public void setEvent(Event event) { this.event = event; }
    public List<ExpenseCategory> getCategories() { return categories; }
    public void setCategories(List<ExpenseCategory> categories) { this.categories = categories; }
}

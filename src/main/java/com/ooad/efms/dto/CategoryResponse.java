package com.ooad.efms.dto;

import com.ooad.efms.model.ExpenseCategory;

import java.math.BigDecimal;

public class CategoryResponse {

    private Long id;
    private String name;
    private BigDecimal allocatedAmount;
    private BigDecimal spentAmount;

    public static CategoryResponse from(ExpenseCategory c) {
        CategoryResponse r = new CategoryResponse();
        r.id = c.getId();
        r.name = c.getName();
        r.allocatedAmount = c.getAllocatedAmount();
        r.spentAmount = c.getSpentAmount();
        return r;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public BigDecimal getSpentAmount() { return spentAmount; }
}

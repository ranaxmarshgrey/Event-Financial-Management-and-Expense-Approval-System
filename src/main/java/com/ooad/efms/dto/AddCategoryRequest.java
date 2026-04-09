package com.ooad.efms.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public class AddCategoryRequest {

    @NotBlank
    private String name;

    @NotNull
    @DecimalMin(value = "0.01", message = "allocatedAmount must be greater than 0")
    private BigDecimal allocatedAmount;

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public BigDecimal getAllocatedAmount() { return allocatedAmount; }
    public void setAllocatedAmount(BigDecimal allocatedAmount) { this.allocatedAmount = allocatedAmount; }
}

package br.com.consultorio.budget.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

public record BudgetItemRequest(
        @NotBlank String description,
        String tooth,
        @Min(1) int quantity,
        @NotNull @DecimalMin("0") BigDecimal unitValue
) {}

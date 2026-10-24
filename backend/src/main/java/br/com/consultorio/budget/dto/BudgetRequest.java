package br.com.consultorio.budget.dto;

import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BudgetRequest(
        UUID dentistId,
        String notes,
        BigDecimal totalValue,
        LocalDate validUntil,
        @Valid List<BudgetItemRequest> items
) {}

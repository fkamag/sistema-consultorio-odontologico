package br.com.consultorio.budget.dto;

import br.com.consultorio.budget.entity.BudgetItem;

import java.math.BigDecimal;
import java.util.UUID;

public record BudgetItemResponse(
        UUID id,
        UUID budgetId,
        String description,
        String tooth,
        int quantity,
        BigDecimal unitValue
) {
    public static BudgetItemResponse from(BudgetItem i) {
        return new BudgetItemResponse(
                i.getId(),
                i.getBudgetId(),
                i.getDescription(),
                i.getTooth(),
                i.getQuantity(),
                i.getUnitValue()
        );
    }
}

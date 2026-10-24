package br.com.consultorio.budget.dto;

import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.entity.BudgetItem;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public record BudgetResponse(
        UUID id,
        UUID patientId,
        UUID dentistId,
        String dentistName,
        Budget.BudgetStatus status,
        String notes,
        LocalDate validUntil,
        BigDecimal totalValue,
        LocalDateTime createdAt,
        String createdByName,
        LocalDateTime updatedAt,
        String updatedByName,
        List<BudgetItemResponse> items
) {
    public static BudgetResponse from(Budget b, List<BudgetItem> items) {
        return new BudgetResponse(
                b.getId(),
                b.getPatientId(),
                b.getDentistId(),
                b.getDentistName(),
                b.getStatus(),
                b.getNotes(),
                b.getValidUntil(),
                b.getTotalValue(),
                b.getCreatedAt(),
                b.getCreatedByName(),
                b.getUpdatedAt(),
                b.getUpdatedByName(),
                items.stream().map(BudgetItemResponse::from).toList()
        );
    }
}

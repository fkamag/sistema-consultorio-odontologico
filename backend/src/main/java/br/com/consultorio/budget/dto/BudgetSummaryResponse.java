package br.com.consultorio.budget.dto;

import br.com.consultorio.budget.entity.Budget;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record BudgetSummaryResponse(
        UUID id,
        UUID patientId,
        String patientName,
        UUID dentistId,
        String dentistName,
        Budget.BudgetStatus status,
        String descricaoItens,
        String notes,
        LocalDate validUntil,
        BigDecimal totalValue,
        BigDecimal totalPago,
        BigDecimal saldo,
        Budget.TratamentoStatus statusTratamento,
        LocalDateTime createdAt,
        String createdByName
) {
    public static BudgetSummaryResponse from(Budget b, String patientName, BigDecimal totalPago) {
        BigDecimal saldo = b.getTotalValue().subtract(totalPago).max(BigDecimal.ZERO);
        return new BudgetSummaryResponse(
                b.getId(), b.getPatientId(), patientName,
                b.getDentistId(), b.getDentistName(),
                b.getStatus(), b.getDescricaoItens(), b.getNotes(), b.getValidUntil(),
                b.getTotalValue(), totalPago, saldo,
                b.getStatusTratamento(),
                b.getCreatedAt(), b.getCreatedByName()
        );
    }
}

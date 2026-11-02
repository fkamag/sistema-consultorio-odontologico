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
        String descricaoItens,
        String notes,
        LocalDate validUntil,
        BigDecimal totalValue,
        BigDecimal totalPago,
        BigDecimal saldo,
        Budget.TratamentoStatus statusTratamento,
        String situacaoSaldo,
        String declaracaoIr,
        LocalDate dataDeclaracao,
        LocalDateTime createdAt,
        String createdByName,
        LocalDateTime updatedAt,
        String updatedByName,
        List<BudgetItemResponse> items
) {
    public static BudgetResponse from(Budget b, List<BudgetItem> items, BigDecimal totalPago) {
        BigDecimal saldo = b.getTotalValue().subtract(totalPago).max(BigDecimal.ZERO);
        return new BudgetResponse(
                b.getId(), b.getPatientId(), b.getDentistId(), b.getDentistName(),
                b.getStatus(), b.getDescricaoItens(), b.getNotes(), b.getValidUntil(),
                b.getTotalValue(), totalPago, saldo,
                b.getStatusTratamento(), b.getSituacaoSaldo(), b.getDeclaracaoIr(), b.getDataDeclaracao(),
                b.getCreatedAt(), b.getCreatedByName(), b.getUpdatedAt(), b.getUpdatedByName(),
                items.stream().map(BudgetItemResponse::from).toList()
        );
    }

    public static BudgetResponse from(Budget b, List<BudgetItem> items) {
        return from(b, items, BigDecimal.ZERO);
    }
}

package br.com.consultorio.budget.dto;

import br.com.consultorio.budget.entity.Budget;
import jakarta.validation.Valid;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record BudgetRequest(
        UUID dentistId,
        String descricaoItens,
        String notes,
        BigDecimal totalValue,
        LocalDate validUntil,
        Budget.BudgetStatus status,
        Budget.TratamentoStatus statusTratamento,
        String situacaoSaldo,
        String declaracaoIr,
        LocalDate dataDeclaracao,
        @Valid List<BudgetItemRequest> items
) {}

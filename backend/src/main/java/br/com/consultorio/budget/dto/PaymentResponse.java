package br.com.consultorio.budget.dto;

import br.com.consultorio.budget.entity.BudgetPayment;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaymentResponse(
        UUID id,
        UUID budgetId,
        LocalDate data,
        BigDecimal valor,
        String formaPagamento,
        BudgetPayment.PaymentStatus status,
        LocalDateTime createdAt
) {
    public static PaymentResponse from(BudgetPayment p) {
        return new PaymentResponse(p.getId(), p.getBudgetId(), p.getData(),
                p.getValor(), p.getFormaPagamento(), p.getStatus(), p.getCreatedAt());
    }
}

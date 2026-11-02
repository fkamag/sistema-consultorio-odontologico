package br.com.consultorio.budget.dto;

import br.com.consultorio.budget.entity.BudgetPayment;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

public record PaymentRequest(
        @NotNull LocalDate data,
        @NotNull @Positive BigDecimal valor,
        @NotNull String formaPagamento,
        @NotNull BudgetPayment.PaymentStatus status
) {}

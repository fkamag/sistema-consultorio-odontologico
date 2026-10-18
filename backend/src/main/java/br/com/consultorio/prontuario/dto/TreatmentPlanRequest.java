package br.com.consultorio.prontuario.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;

public record TreatmentPlanRequest(
        @NotBlank(message = "Descrição é obrigatória.")
        @Size(max = 300)
        String description,

        String tooth,

        @DecimalMin(value = "0.0", message = "Valor não pode ser negativo.")
        BigDecimal valueEstimated
) {}

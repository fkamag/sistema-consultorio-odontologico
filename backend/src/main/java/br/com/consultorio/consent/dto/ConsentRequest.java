package br.com.consultorio.consent.dto;

import br.com.consultorio.consent.entity.Consent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ConsentRequest(
        @NotBlank String title,
        @NotNull Consent.ConsentType type,
        @NotBlank String content
) {}

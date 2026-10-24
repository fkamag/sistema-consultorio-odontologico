package br.com.consultorio.odontogram.dto;

import jakarta.validation.constraints.NotBlank;

public record ToothRecordRequest(
        @NotBlank String toothNumber,
        String vestibular,
        String mesial,
        String distal,
        String lingual,
        String oclusal,
        boolean canal,
        boolean ausente,
        String note
) {}

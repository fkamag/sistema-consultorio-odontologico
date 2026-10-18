package br.com.consultorio.prontuario.dto;

import br.com.consultorio.prontuario.entity.ClinicalNote.NoteType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;

import java.time.LocalDate;
import java.util.UUID;

public record ClinicalNoteRequest(
        @NotNull(message = "Data é obrigatória.")
        @PastOrPresent(message = "A data não pode ser futura.")
        LocalDate noteDate,

        @NotNull(message = "Tipo é obrigatório.")
        NoteType noteType,

        String tooth,

        @NotBlank(message = "Descrição é obrigatória.")
        String description,

        UUID appointmentId
) {}

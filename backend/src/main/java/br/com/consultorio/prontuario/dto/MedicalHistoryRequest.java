package br.com.consultorio.prontuario.dto;

public record MedicalHistoryRequest(
        String alergias,
        String medicamentos,
        boolean diabetes,
        boolean hipertensao,
        boolean problemasCardiacos,
        String tratamentosMedicos,
        String cirurgias,
        String observacoes
) {}

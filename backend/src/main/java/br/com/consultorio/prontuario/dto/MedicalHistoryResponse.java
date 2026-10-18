package br.com.consultorio.prontuario.dto;

import br.com.consultorio.prontuario.entity.MedicalHistory;

import java.time.LocalDateTime;
import java.util.UUID;

public record MedicalHistoryResponse(
        UUID id,
        UUID patientId,
        int versionNumber,
        String alergias,
        String medicamentos,
        boolean diabetes,
        boolean hipertensao,
        boolean problemasCardiacos,
        String tratamentosMedicos,
        String cirurgias,
        String observacoes,
        LocalDateTime createdAt,
        String createdByName
) {
    public static MedicalHistoryResponse from(MedicalHistory h) {
        return new MedicalHistoryResponse(
                h.getId(), h.getPatientId(), h.getVersionNumber(),
                h.getAlergias(), h.getMedicamentos(),
                h.isDiabetes(), h.isHipertensao(), h.isProblemasCardiacos(),
                h.getTratamentosMedicos(), h.getCirurgias(), h.getObservacoes(),
                h.getCreatedAt(), h.getCreatedByName()
        );
    }
}

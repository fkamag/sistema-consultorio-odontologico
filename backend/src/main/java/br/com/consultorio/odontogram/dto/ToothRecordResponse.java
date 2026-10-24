package br.com.consultorio.odontogram.dto;

import br.com.consultorio.odontogram.entity.ToothRecord;

import java.time.LocalDateTime;
import java.util.UUID;

public record ToothRecordResponse(
        UUID id,
        UUID patientId,
        String toothNumber,
        String vestibular,
        String mesial,
        String distal,
        String lingual,
        String oclusal,
        boolean canal,
        boolean ausente,
        String note,
        LocalDateTime updatedAt,
        String updatedByName
) {
    public static ToothRecordResponse from(ToothRecord t) {
        return new ToothRecordResponse(
                t.getId(),
                t.getPatientId(),
                t.getToothNumber(),
                t.getVestibular(),
                t.getMesial(),
                t.getDistal(),
                t.getLingual(),
                t.getOclusal(),
                t.isCanal(),
                t.isAusente(),
                t.getNote(),
                t.getUpdatedAt(),
                t.getUpdatedByName()
        );
    }
}

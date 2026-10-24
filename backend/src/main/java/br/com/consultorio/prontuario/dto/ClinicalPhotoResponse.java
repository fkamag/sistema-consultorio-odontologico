package br.com.consultorio.prontuario.dto;

import br.com.consultorio.prontuario.entity.ClinicalPhoto;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClinicalPhotoResponse(
        UUID id,
        UUID patientId,
        UUID clinicalNoteId,
        String caption,
        LocalDateTime createdAt,
        String createdByName
) {
    public static ClinicalPhotoResponse from(ClinicalPhoto p) {
        return new ClinicalPhotoResponse(p.getId(), p.getPatientId(), p.getClinicalNoteId(),
                p.getCaption(), p.getCreatedAt(), p.getCreatedByName());
    }
}

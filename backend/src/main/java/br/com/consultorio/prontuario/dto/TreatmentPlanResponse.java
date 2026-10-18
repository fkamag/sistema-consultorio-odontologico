package br.com.consultorio.prontuario.dto;

import br.com.consultorio.prontuario.entity.TreatmentPlanItem;
import br.com.consultorio.prontuario.entity.TreatmentPlanItem.Status;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record TreatmentPlanResponse(
        UUID id,
        UUID patientId,
        String description,
        String tooth,
        BigDecimal valueEstimated,
        Status status,
        UUID appointmentId,
        LocalDateTime createdAt,
        String createdByName,
        LocalDateTime updatedAt,
        String updatedByName
) {
    public static TreatmentPlanResponse from(TreatmentPlanItem item) {
        return new TreatmentPlanResponse(
                item.getId(), item.getPatientId(),
                item.getDescription(), item.getTooth(), item.getValueEstimated(),
                item.getStatus(), item.getAppointmentId(),
                item.getCreatedAt(), item.getCreatedByName(),
                item.getUpdatedAt(), item.getUpdatedByName()
        );
    }
}

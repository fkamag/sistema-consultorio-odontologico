package br.com.consultorio.consent.dto;

import br.com.consultorio.consent.entity.Consent;

import java.time.LocalDateTime;
import java.util.UUID;

public record ConsentResponse(
        UUID id,
        UUID patientId,
        String title,
        Consent.ConsentType type,
        String content,
        LocalDateTime signedAt,
        String signedByName,
        LocalDateTime createdAt,
        String createdByName
) {
    public static ConsentResponse from(Consent c) {
        return new ConsentResponse(
                c.getId(),
                c.getPatientId(),
                c.getTitle(),
                c.getType(),
                c.getContent(),
                c.getSignedAt(),
                c.getSignedByName(),
                c.getCreatedAt(),
                c.getCreatedByName()
        );
    }
}

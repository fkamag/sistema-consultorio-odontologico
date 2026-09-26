package br.com.consultorio.patient.dto;

import br.com.consultorio.patient.entity.Patient;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record PatientResponse(
        UUID id,
        String name,
        String cpf,
        LocalDate birthDate,
        String phone,
        String email,
        String street,
        String number,
        String complement,
        String neighborhood,
        String city,
        String state,
        String notes,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static PatientResponse from(Patient p) {
        return new PatientResponse(
                p.getId(),
                p.getName(),
                p.getCpf(),
                p.getBirthDate(),
                p.getPhone(),
                p.getEmail(),
                p.getStreet(),
                p.getNumber(),
                p.getComplement(),
                p.getNeighborhood(),
                p.getCity(),
                p.getState(),
                p.getNotes(),
                p.isActive(),
                p.getCreatedAt(),
                p.getUpdatedAt()
        );
    }
}

package br.com.consultorio.prontuario.dto;

import br.com.consultorio.prontuario.entity.ClinicalNote;
import br.com.consultorio.prontuario.entity.ClinicalNote.NoteType;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClinicalNoteResponse(
        UUID id,
        UUID patientId,
        UUID dentistId,
        String dentistName,
        UUID appointmentId,
        LocalDate noteDate,
        NoteType noteType,
        String tooth,
        String description,
        LocalDateTime createdAt,
        String createdByName
) {
    public static ClinicalNoteResponse from(ClinicalNote n, String dentistName) {
        return new ClinicalNoteResponse(
                n.getId(), n.getPatientId(), n.getDentistId(), dentistName,
                n.getAppointmentId(), n.getNoteDate(), n.getNoteType(),
                n.getTooth(), n.getDescription(),
                n.getCreatedAt(), n.getCreatedByName()
        );
    }
}

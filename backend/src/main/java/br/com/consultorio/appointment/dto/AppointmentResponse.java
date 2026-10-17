package br.com.consultorio.appointment.dto;

import br.com.consultorio.appointment.entity.Appointment;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentResponse(
        UUID id,
        UUID patientId,
        String patientName,
        UUID dentistId,
        String dentistName,
        LocalDateTime startTime,
        LocalDateTime endTime,
        int durationMinutes,
        String status,
        String notes,
        LocalDateTime createdAt,
        String createdByName,
        String updatedByName
) {
    public static AppointmentResponse from(Appointment a, String patientName, String dentistName) {
        return new AppointmentResponse(
                a.getId(),
                a.getPatientId(),
                patientName,
                a.getDentistId(),
                dentistName,
                a.getStartTime(),
                a.endTime(),
                a.getDurationMinutes(),
                a.getStatus().name(),
                a.getNotes(),
                a.getCreatedAt(),
                a.getCreatedByName(),
                a.getUpdatedByName()
        );
    }
}

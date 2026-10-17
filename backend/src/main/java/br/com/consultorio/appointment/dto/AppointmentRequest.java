package br.com.consultorio.appointment.dto;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentRequest(

        @NotNull(message = "Paciente é obrigatório.")
        UUID patientId,

        @NotNull(message = "Dentista é obrigatório.")
        UUID dentistId,

        @NotNull(message = "Data e hora são obrigatórios.")
        @Future(message = "O agendamento deve ser em uma data futura.")
        LocalDateTime startTime,

        @Min(value = 10, message = "Duração mínima é 10 minutos.")
        @Max(value = 480, message = "Duração máxima é 480 minutos.")
        Integer durationMinutes,

        String notes
) {
    public int duration() {
        return durationMinutes != null ? durationMinutes : 30;
    }
}

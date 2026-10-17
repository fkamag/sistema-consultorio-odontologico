package br.com.consultorio.appointment.dto;

import jakarta.validation.constraints.Future;
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

        String notes
) {}

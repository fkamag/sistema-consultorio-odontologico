package br.com.consultorio.patient.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import java.time.LocalDate;

public record PatientRequest(

        @NotBlank(message = "Nome é obrigatório")
        @Size(max = 150)
        String name,

        @NotBlank(message = "CPF é obrigatório")
        @Pattern(regexp = "\\d{3}\\.\\d{3}\\.\\d{3}-\\d{2}", message = "CPF deve estar no formato 000.000.000-00")
        String cpf,

        LocalDate birthDate,

        @Size(max = 20)
        String phone,

        @Size(max = 150)
        String email,

        @Size(max = 200)
        String street,

        @Size(max = 20)
        String number,

        @Size(max = 100)
        String complement,

        @Size(max = 100)
        String neighborhood,

        @Size(max = 100)
        String city,

        @Size(max = 2)
        String state,

        String notes
) {}

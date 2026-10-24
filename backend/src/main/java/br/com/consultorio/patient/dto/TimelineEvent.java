package br.com.consultorio.patient.dto;

import java.time.LocalDate;

public record TimelineEvent(
        String type,
        LocalDate date,
        String description,
        String detail,
        String createdByName
) {}

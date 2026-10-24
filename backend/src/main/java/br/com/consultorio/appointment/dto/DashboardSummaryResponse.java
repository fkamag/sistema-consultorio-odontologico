package br.com.consultorio.appointment.dto;

import java.util.List;

public record DashboardSummaryResponse(
        long consultasHoje,
        long confirmadosHoje,
        long canceladasHoje,
        long pacientesAtivos,
        long orcamentosEmAberto,
        List<AppointmentResponse> proximasConsultasHoje
) {}

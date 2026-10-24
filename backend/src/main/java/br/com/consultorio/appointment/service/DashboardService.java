package br.com.consultorio.appointment.service;

import br.com.consultorio.appointment.dto.AppointmentResponse;
import br.com.consultorio.appointment.dto.DashboardSummaryResponse;
import br.com.consultorio.appointment.entity.Appointment;
import br.com.consultorio.appointment.repository.AppointmentRepository;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.repository.BudgetRepository;
import br.com.consultorio.patient.repository.PatientRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final AppointmentRepository appointmentRepository;
    private final PatientRepository patientRepository;
    private final AppUserRepository userRepository;
    private final BudgetRepository budgetRepository;

    public DashboardSummaryResponse getSummary() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay   = startOfDay.plusDays(1);

        long consultasHoje = appointmentRepository.countByStartTimeBetween(startOfDay, endOfDay);
        long confirmadosHoje = appointmentRepository.countByStartTimeBetweenAndStatus(
                startOfDay, endOfDay, Appointment.Status.CONFIRMADO);
        long canceladasHoje = appointmentRepository.countByStartTimeBetweenAndStatus(
                startOfDay, endOfDay, Appointment.Status.CANCELADO);
        long pacientesAtivos = patientRepository.countByDeletedAtIsNull();
        long orcamentosEmAberto = budgetRepository.countByStatusIn(
                List.of(Budget.BudgetStatus.RASCUNHO, Budget.BudgetStatus.ENVIADO));

        List<AppointmentResponse> proximas = appointmentRepository
                .findByStartTimeBetweenOrderByStartTime(startOfDay, endOfDay)
                .stream()
                .filter(a -> a.getStatus() != Appointment.Status.CANCELADO)
                .map(a -> {
                    String patientName = patientRepository.findByIdAndDeletedAtIsNull(a.getPatientId())
                            .map(p -> p.getName()).orElse("—");
                    String dentistName = userRepository.findById(a.getDentistId())
                            .map(u -> u.getName()).orElse("—");
                    return AppointmentResponse.from(a, patientName, dentistName);
                })
                .toList();

        return new DashboardSummaryResponse(
                consultasHoje,
                confirmadosHoje,
                canceladasHoje,
                pacientesAtivos,
                orcamentosEmAberto,
                proximas
        );
    }
}

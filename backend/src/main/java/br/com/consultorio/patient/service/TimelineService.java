package br.com.consultorio.patient.service;

import br.com.consultorio.appointment.entity.Appointment;
import br.com.consultorio.appointment.repository.AppointmentRepository;
import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.repository.BudgetRepository;
import br.com.consultorio.consent.repository.ConsentRepository;
import br.com.consultorio.patient.dto.TimelineEvent;
import br.com.consultorio.prontuario.repository.ClinicalNoteRepository;
import br.com.consultorio.prontuario.repository.MedicalHistoryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final AppointmentRepository appointmentRepository;
    private final ClinicalNoteRepository clinicalNoteRepository;
    private final MedicalHistoryRepository medicalHistoryRepository;
    private final BudgetRepository budgetRepository;
    private final ConsentRepository consentRepository;

    public List<TimelineEvent> timeline(UUID patientId) {
        List<TimelineEvent> events = new ArrayList<>();

        appointmentRepository.findByPatientIdOrderByStartTimeDesc(patientId).stream()
                .filter(a -> a.getStatus() == Appointment.Status.REALIZADO)
                .forEach(a -> events.add(new TimelineEvent(
                        "CONSULTA",
                        a.getStartTime().toLocalDate(),
                        "Consulta realizada",
                        a.getNotes(),
                        a.getCreatedByName()
                )));

        clinicalNoteRepository.findByPatientIdOrderByNoteDateDescCreatedAtDesc(patientId)
                .forEach(n -> events.add(new TimelineEvent(
                        "ANOTACAO_CLINICA",
                        n.getNoteDate(),
                        n.getNoteType().name(),
                        n.getDescription(),
                        n.getCreatedByName()
                )));

        medicalHistoryRepository.findByPatientIdOrderByVersionNumberDesc(patientId)
                .forEach(m -> events.add(new TimelineEvent(
                        "ANAMNESE",
                        m.getCreatedAt().toLocalDate(),
                        "Anamnese v" + m.getVersionNumber(),
                        null,
                        m.getCreatedByName()
                )));

        budgetRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .forEach(b -> events.add(new TimelineEvent(
                        "ORCAMENTO",
                        b.getCreatedAt().toLocalDate(),
                        "Orçamento " + b.getStatus().name(),
                        b.getNotes(),
                        b.getCreatedByName()
                )));

        consentRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .forEach(c -> events.add(new TimelineEvent(
                        "CONSENTIMENTO",
                        c.getCreatedAt().toLocalDate(),
                        c.getTitle(),
                        c.getSignedAt() != null ? "Assinado por: " + c.getSignedByName() : null,
                        c.getCreatedByName()
                )));

        events.sort(Comparator.comparing(TimelineEvent::date).reversed());
        return events;
    }
}

package br.com.consultorio.appointment.service;

import br.com.consultorio.appointment.dto.AppointmentRequest;
import br.com.consultorio.appointment.dto.AppointmentResponse;
import br.com.consultorio.appointment.entity.Appointment;
import br.com.consultorio.appointment.repository.AppointmentRepository;
import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.patient.entity.Patient;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AppointmentService {

    private final AppointmentRepository repository;
    private final PatientRepository patientRepository;
    private final AppUserRepository userRepository;
    private final AuditService auditService;

    @Transactional
    public AppointmentResponse create(AppointmentRequest request, String ip) {
        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(request.patientId())
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado."));
        AppUser dentist = userRepository.findById(request.dentistId())
                .orElseThrow(() -> new NotFoundException("Dentista não encontrado."));

        LocalDateTime end = request.startTime().plusMinutes(30);
        if (repository.existsConflict(request.dentistId(), request.startTime(), end, null)) {
            throw new ConflictException("Já existe um agendamento neste horário para o dentista selecionado.");
        }

        AppUser actor = currentUser();
        Appointment appointment = Appointment.builder()
                .patientId(patient.getId())
                .dentistId(dentist.getId())
                .startTime(request.startTime())
                .notes(request.notes())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .updatedById(actor != null ? actor.getId() : null)
                .updatedByName(actor != null ? actor.getName() : null)
                .build();

        Appointment saved = repository.save(appointment);
        auditService.log(AuditLog.Action.CREATE, "APPOINTMENT", saved.getId(),
                "Agendamento criado: " + patient.getName() + " às " + saved.getStartTime(), ip);

        return AppointmentResponse.from(saved, patient.getName(), dentist.getName());
    }

    @Transactional
    public AppointmentResponse update(UUID id, AppointmentRequest request, String ip) {
        Appointment appointment = getOrThrow(id);
        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(request.patientId())
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado."));
        AppUser dentist = userRepository.findById(request.dentistId())
                .orElseThrow(() -> new NotFoundException("Dentista não encontrado."));

        LocalDateTime end = request.startTime().plusMinutes(30);
        if (repository.existsConflict(request.dentistId(), request.startTime(), end, id)) {
            throw new ConflictException("Já existe um agendamento neste horário para o dentista selecionado.");
        }

        AppUser actor = currentUser();
        appointment.setPatientId(patient.getId());
        appointment.setDentistId(dentist.getId());
        appointment.setStartTime(request.startTime());
        appointment.setNotes(request.notes());
        appointment.setUpdatedById(actor != null ? actor.getId() : null);
        appointment.setUpdatedByName(actor != null ? actor.getName() : null);

        Appointment saved = repository.save(appointment);
        auditService.log(AuditLog.Action.UPDATE, "APPOINTMENT", id,
                "Agendamento alterado: " + patient.getName() + " às " + saved.getStartTime(), ip);

        return AppointmentResponse.from(saved, patient.getName(), dentist.getName());
    }

    @Transactional
    public AppointmentResponse changeStatus(UUID id, Appointment.Status novoStatus, String ip) {
        Appointment appointment = getOrThrow(id);
        validateStatusTransition(appointment.getStatus(), novoStatus);

        AppUser actor = currentUser();
        appointment.setStatus(novoStatus);
        appointment.setUpdatedById(actor != null ? actor.getId() : null);
        appointment.setUpdatedByName(actor != null ? actor.getName() : null);

        Appointment saved = repository.save(appointment);

        Patient patient = patientRepository.findByIdAndDeletedAtIsNull(saved.getPatientId()).orElse(null);
        AppUser dentist = userRepository.findById(saved.getDentistId()).orElse(null);

        auditService.log(AuditLog.Action.UPDATE, "APPOINTMENT", id,
                "Status alterado para " + novoStatus, ip);

        return AppointmentResponse.from(saved,
                patient != null ? patient.getName() : "",
                dentist != null ? dentist.getName() : "");
    }

    public List<AppointmentResponse> findByDentistAndPeriod(UUID dentistId, LocalDateTime from, LocalDateTime to) {
        return repository.findByDentistIdAndStartTimeBetweenOrderByStartTime(dentistId, from, to)
                .stream()
                .map(a -> {
                    Patient patient = patientRepository.findByIdAndDeletedAtIsNull(a.getPatientId()).orElse(null);
                    AppUser dentist = userRepository.findById(a.getDentistId()).orElse(null);
                    return AppointmentResponse.from(a,
                            patient != null ? patient.getName() : "",
                            dentist != null ? dentist.getName() : "");
                })
                .toList();
    }

    public List<AppointmentResponse> findByPatient(UUID patientId) {
        AppUser dentist = null;
        return repository.findByPatientIdOrderByStartTimeDesc(patientId)
                .stream()
                .map(a -> {
                    Patient patient = patientRepository.findByIdAndDeletedAtIsNull(a.getPatientId()).orElse(null);
                    AppUser d = userRepository.findById(a.getDentistId()).orElse(null);
                    return AppointmentResponse.from(a,
                            patient != null ? patient.getName() : "",
                            d != null ? d.getName() : "");
                })
                .toList();
    }

    private Appointment getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Agendamento não encontrado."));
    }

    private void validateStatusTransition(Appointment.Status atual, Appointment.Status novo) {
        boolean invalido = switch (atual) {
            case REALIZADO, CANCELADO -> true;
            case CONFIRMADO -> novo == Appointment.Status.AGENDADO;
            default -> false;
        };
        if (invalido) {
            throw new ConflictException(
                    "Não é possível alterar o status de um agendamento com situação " + atual + ".");
        }
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

package br.com.consultorio.patient.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.patient.dto.PatientRequest;
import br.com.consultorio.patient.dto.PatientResponse;
import br.com.consultorio.patient.entity.Patient;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository repository;
    private final AuditService auditService;

    public Page<PatientResponse> list(String search, int page, int size) {
        return repository.search(search, PageRequest.of(page, size))
                .map(PatientResponse::from);
    }

    public PatientResponse findById(UUID id) {
        return PatientResponse.from(getOrThrow(id));
    }

    @Transactional
    public PatientResponse create(PatientRequest request, String ip) {
        if (repository.existsByCpfAndDeletedAtIsNull(request.cpf())) {
            throw new ConflictException("Já existe um paciente cadastrado com este CPF.");
        }
        AppUser actor = currentUser();
        Patient patient = Patient.builder()
                .name(request.name())
                .cpf(request.cpf())
                .birthDate(request.birthDate())
                .phone(request.phone())
                .email(request.email())
                .street(request.street())
                .number(request.number())
                .complement(request.complement())
                .neighborhood(request.neighborhood())
                .city(request.city())
                .state(request.state())
                .notes(request.notes())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .updatedById(actor != null ? actor.getId() : null)
                .updatedByName(actor != null ? actor.getName() : null)
                .build();
        Patient saved = repository.save(patient);
        auditService.log(AuditLog.Action.CREATE, "PATIENT", saved.getId(),
                "Paciente criado: " + saved.getName(), ip);
        return PatientResponse.from(saved);
    }

    @Transactional
    public PatientResponse update(UUID id, PatientRequest request, String ip) {
        Patient patient = getOrThrow(id);
        if (repository.existsByCpfAndIdNotAndDeletedAtIsNull(request.cpf(), id)) {
            throw new ConflictException("Já existe outro paciente cadastrado com este CPF.");
        }
        AppUser actor = currentUser();
        patient.setName(request.name());
        patient.setCpf(request.cpf());
        patient.setBirthDate(request.birthDate());
        patient.setPhone(request.phone());
        patient.setEmail(request.email());
        patient.setStreet(request.street());
        patient.setNumber(request.number());
        patient.setComplement(request.complement());
        patient.setNeighborhood(request.neighborhood());
        patient.setCity(request.city());
        patient.setState(request.state());
        patient.setNotes(request.notes());
        patient.setUpdatedById(actor != null ? actor.getId() : null);
        patient.setUpdatedByName(actor != null ? actor.getName() : null);
        Patient saved = repository.save(patient);
        auditService.log(AuditLog.Action.UPDATE, "PATIENT", saved.getId(),
                "Paciente atualizado: " + saved.getName(), ip);
        return PatientResponse.from(saved);
    }

    @Transactional
    public void delete(UUID id, String ip) {
        Patient patient = getOrThrow(id);
        AppUser actor = currentUser();
        patient.setDeletedAt(LocalDateTime.now());
        patient.setActive(false);
        patient.setUpdatedById(actor != null ? actor.getId() : null);
        patient.setUpdatedByName(actor != null ? actor.getName() : null);
        repository.save(patient);
        auditService.log(AuditLog.Action.DELETE, "PATIENT", id,
                "Paciente removido: " + patient.getName(), ip);
    }

    private Patient getOrThrow(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado."));
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

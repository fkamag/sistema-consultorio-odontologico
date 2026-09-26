package br.com.consultorio.patient.service;

import br.com.consultorio.patient.dto.PatientRequest;
import br.com.consultorio.patient.dto.PatientResponse;
import br.com.consultorio.patient.entity.Patient;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PatientService {

    private final PatientRepository repository;

    public Page<PatientResponse> list(String search, int page, int size) {
        return repository.search(search, PageRequest.of(page, size))
                .map(PatientResponse::from);
    }

    public PatientResponse findById(UUID id) {
        return PatientResponse.from(getOrThrow(id));
    }

    @Transactional
    public PatientResponse create(PatientRequest request) {
        if (repository.existsByCpfAndDeletedAtIsNull(request.cpf())) {
            throw new ConflictException("Já existe um paciente cadastrado com este CPF.");
        }
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
                .build();
        return PatientResponse.from(repository.save(patient));
    }

    @Transactional
    public PatientResponse update(UUID id, PatientRequest request) {
        Patient patient = getOrThrow(id);
        if (repository.existsByCpfAndIdNotAndDeletedAtIsNull(request.cpf(), id)) {
            throw new ConflictException("Já existe outro paciente cadastrado com este CPF.");
        }
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
        return PatientResponse.from(repository.save(patient));
    }

    @Transactional
    public void delete(UUID id) {
        Patient patient = getOrThrow(id);
        patient.setDeletedAt(LocalDateTime.now());
        patient.setActive(false);
        repository.save(patient);
    }

    private Patient getOrThrow(UUID id) {
        return repository.findByIdAndDeletedAtIsNull(id)
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado."));
    }
}

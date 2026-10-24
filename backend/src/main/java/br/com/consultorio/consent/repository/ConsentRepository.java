package br.com.consultorio.consent.repository;

import br.com.consultorio.consent.entity.Consent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ConsentRepository extends JpaRepository<Consent, UUID> {
    List<Consent> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    Optional<Consent> findByIdAndPatientId(UUID id, UUID patientId);
}

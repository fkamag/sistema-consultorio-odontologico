package br.com.consultorio.prontuario.repository;

import br.com.consultorio.prontuario.entity.MedicalHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface MedicalHistoryRepository extends JpaRepository<MedicalHistory, UUID> {
    List<MedicalHistory> findByPatientIdOrderByVersionNumberDesc(UUID patientId);
    Optional<MedicalHistory> findTopByPatientIdOrderByVersionNumberDesc(UUID patientId);
}

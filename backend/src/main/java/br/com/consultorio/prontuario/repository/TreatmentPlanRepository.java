package br.com.consultorio.prontuario.repository;

import br.com.consultorio.prontuario.entity.TreatmentPlanItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface TreatmentPlanRepository extends JpaRepository<TreatmentPlanItem, UUID> {
    List<TreatmentPlanItem> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
}

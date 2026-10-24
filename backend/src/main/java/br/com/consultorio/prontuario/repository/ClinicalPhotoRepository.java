package br.com.consultorio.prontuario.repository;

import br.com.consultorio.prontuario.entity.ClinicalPhoto;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface ClinicalPhotoRepository extends JpaRepository<ClinicalPhoto, UUID> {
    List<ClinicalPhoto> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    List<ClinicalPhoto> findByClinicalNoteIdOrderByCreatedAtDesc(UUID clinicalNoteId);
}

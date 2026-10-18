package br.com.consultorio.prontuario.repository;

import br.com.consultorio.prontuario.entity.ClinicalNote;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ClinicalNoteRepository extends JpaRepository<ClinicalNote, UUID> {
    List<ClinicalNote> findByPatientIdOrderByNoteDateDescCreatedAtDesc(UUID patientId);
}

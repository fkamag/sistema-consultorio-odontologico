package br.com.consultorio.odontogram.repository;

import br.com.consultorio.odontogram.entity.ToothRecord;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ToothRecordRepository extends JpaRepository<ToothRecord, UUID> {
    List<ToothRecord> findByPatientId(UUID patientId);
    Optional<ToothRecord> findByPatientIdAndToothNumber(UUID patientId, String toothNumber);
}

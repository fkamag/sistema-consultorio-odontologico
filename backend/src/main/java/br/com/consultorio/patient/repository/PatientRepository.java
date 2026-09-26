package br.com.consultorio.patient.repository;

import br.com.consultorio.patient.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface PatientRepository extends JpaRepository<Patient, UUID> {

    @Query("""
            SELECT p FROM Patient p
            WHERE p.deletedAt IS NULL
              AND (:search IS NULL OR :search = ''
                   OR LOWER(p.name) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR REPLACE(REPLACE(REPLACE(p.cpf, '.', ''), '-', ''), ' ', '')
                      LIKE CONCAT('%', REPLACE(REPLACE(REPLACE(:search, '.', ''), '-', ''), ' ', ''), '%'))
            ORDER BY p.name ASC
            """)
    Page<Patient> search(@Param("search") String search, Pageable pageable);

    Optional<Patient> findByIdAndDeletedAtIsNull(UUID id);

    boolean existsByCpfAndDeletedAtIsNull(String cpf);

    boolean existsByCpfAndIdNotAndDeletedAtIsNull(String cpf, UUID id);
}

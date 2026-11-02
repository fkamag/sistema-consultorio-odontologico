package br.com.consultorio.budget.repository;

import br.com.consultorio.budget.entity.Budget;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    Optional<Budget> findByIdAndPatientId(UUID id, UUID patientId);
    long countByStatusIn(List<Budget.BudgetStatus> statuses);

    @Query("""
            SELECT b FROM Budget b
            WHERE (:status IS NULL OR b.status = :status)
            ORDER BY b.createdAt DESC
            """)
    Page<Budget> searchAll(@Param("status") Budget.BudgetStatus status, Pageable pageable);
}

package br.com.consultorio.budget.repository;

import br.com.consultorio.budget.entity.Budget;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BudgetRepository extends JpaRepository<Budget, UUID> {
    List<Budget> findByPatientIdOrderByCreatedAtDesc(UUID patientId);
    Optional<Budget> findByIdAndPatientId(UUID id, UUID patientId);
    long countByStatusIn(List<Budget.BudgetStatus> statuses);
}

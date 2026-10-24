package br.com.consultorio.budget.repository;

import br.com.consultorio.budget.entity.BudgetItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

public interface BudgetItemRepository extends JpaRepository<BudgetItem, UUID> {
    List<BudgetItem> findByBudgetId(UUID budgetId);

    @Transactional
    void deleteByBudgetId(UUID budgetId);
}

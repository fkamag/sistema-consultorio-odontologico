package br.com.consultorio.budget.repository;

import br.com.consultorio.budget.entity.BudgetPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

public interface BudgetPaymentRepository extends JpaRepository<BudgetPayment, UUID> {

    List<BudgetPayment> findByBudgetIdOrderByDataAsc(UUID budgetId);

    void deleteByBudgetId(UUID budgetId);

    @Query("SELECT COALESCE(SUM(p.valor), 0) FROM BudgetPayment p WHERE p.budgetId = :budgetId AND p.status = 'PAGO'")
    BigDecimal sumPagoByBudgetId(@Param("budgetId") UUID budgetId);

    @Query("SELECT p.budgetId, COALESCE(SUM(p.valor), 0) FROM BudgetPayment p WHERE p.budgetId IN :ids AND p.status = 'PAGO' GROUP BY p.budgetId")
    List<Object[]> sumPagoGroupedByBudgetIds(@Param("ids") List<UUID> ids);
}

package br.com.consultorio.budget.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.budget.dto.BudgetRequest;
import br.com.consultorio.budget.dto.BudgetResponse;
import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.entity.BudgetItem;
import br.com.consultorio.budget.repository.BudgetItemRepository;
import br.com.consultorio.budget.repository.BudgetRepository;
import br.com.consultorio.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final AppUserRepository appUserRepository;
    private final AuditService auditService;

    public List<BudgetResponse> listByPatient(UUID patientId) {
        return budgetRepository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(b -> BudgetResponse.from(b, budgetItemRepository.findByBudgetId(b.getId())))
                .toList();
    }

    public BudgetResponse findById(UUID patientId, UUID budgetId) {
        Budget budget = budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        return BudgetResponse.from(budget, budgetItemRepository.findByBudgetId(budget.getId()));
    }

    @Transactional
    public BudgetResponse create(UUID patientId, BudgetRequest req, String ip) {
        AppUser actor = currentUser();
        String dentistName = req.dentistId() != null
                ? appUserRepository.findById(req.dentistId()).map(AppUser::getName).orElse(null)
                : null;

        Budget budget = Budget.builder()
                .patientId(patientId)
                .dentistId(req.dentistId())
                .dentistName(dentistName)
                .notes(req.notes())
                .validUntil(req.validUntil())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .updatedById(actor != null ? actor.getId() : null)
                .updatedByName(actor != null ? actor.getName() : null)
                .build();

        Budget saved = budgetRepository.save(budget);

        List<BudgetItem> items = req.items() == null ? List.of() : req.items().stream()
                .map(i -> BudgetItem.builder()
                        .budgetId(saved.getId())
                        .description(i.description())
                        .tooth(i.tooth())
                        .quantity(i.quantity())
                        .unitValue(i.unitValue())
                        .build())
                .toList();

        if (!items.isEmpty()) budgetItemRepository.saveAll(items);

        BigDecimal total = items.isEmpty()
                ? (req.totalValue() != null ? req.totalValue() : BigDecimal.ZERO)
                : items.stream()
                        .map(i -> i.getUnitValue().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
        saved.setTotalValue(total);
        budgetRepository.save(saved);

        auditService.log(AuditLog.Action.CREATE, "BUDGET", saved.getId(),
                "Orçamento criado para paciente " + patientId, ip);
        return BudgetResponse.from(saved, items);
    }

    @Transactional
    public BudgetResponse updateStatus(UUID patientId, UUID budgetId, Budget.BudgetStatus status, String ip) {
        Budget budget = budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        AppUser actor = currentUser();
        budget.setStatus(status);
        budget.setUpdatedById(actor != null ? actor.getId() : null);
        budget.setUpdatedByName(actor != null ? actor.getName() : null);
        Budget saved = budgetRepository.save(budget);
        auditService.log(AuditLog.Action.UPDATE, "BUDGET", saved.getId(),
                "Status alterado para " + status, ip);
        return BudgetResponse.from(saved, budgetItemRepository.findByBudgetId(saved.getId()));
    }

    @Transactional
    public void delete(UUID patientId, UUID budgetId, String ip) {
        Budget budget = budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        budgetItemRepository.deleteByBudgetId(budgetId);
        budgetRepository.delete(budget);
        auditService.log(AuditLog.Action.DELETE, "BUDGET", budgetId,
                "Orçamento removido do paciente " + patientId, ip);
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

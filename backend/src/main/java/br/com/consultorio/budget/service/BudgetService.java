package br.com.consultorio.budget.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.budget.dto.*;
import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.entity.BudgetItem;
import br.com.consultorio.budget.entity.BudgetPayment;
import br.com.consultorio.budget.repository.BudgetItemRepository;
import br.com.consultorio.budget.repository.BudgetPaymentRepository;
import br.com.consultorio.budget.repository.BudgetRepository;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class BudgetService {

    private final BudgetRepository budgetRepository;
    private final BudgetItemRepository budgetItemRepository;
    private final BudgetPaymentRepository budgetPaymentRepository;
    private final AppUserRepository appUserRepository;
    private final PatientRepository patientRepository;
    private final AuditService auditService;

    public Page<BudgetSummaryResponse> listAll(Budget.BudgetStatus status, int page, int size) {
        Page<Budget> budgets = budgetRepository.searchAll(status, PageRequest.of(page, size));
        List<UUID> budgetIds = budgets.stream().map(Budget::getId).toList();
        List<UUID> patientIds = budgets.stream().map(Budget::getPatientId).distinct().toList();

        Map<UUID, String> patientNames = patientRepository.findAllById(patientIds).stream()
                .collect(Collectors.toMap(p -> p.getId(), p -> p.getName()));

        Map<UUID, BigDecimal> pagoMap = budgetPaymentRepository.sumPagoGroupedByBudgetIds(budgetIds).stream()
                .collect(Collectors.toMap(r -> (UUID) r[0], r -> (BigDecimal) r[1]));

        return budgets.map(b -> BudgetSummaryResponse.from(b,
                patientNames.getOrDefault(b.getPatientId(), "—"),
                pagoMap.getOrDefault(b.getId(), BigDecimal.ZERO)));
    }

    public List<BudgetResponse> listByPatient(UUID patientId) {
        List<Budget> list = budgetRepository.findByPatientIdOrderByCreatedAtDesc(patientId);
        List<UUID> ids = list.stream().map(Budget::getId).toList();
        Map<UUID, BigDecimal> pagoMap = budgetPaymentRepository.sumPagoGroupedByBudgetIds(ids).stream()
                .collect(Collectors.toMap(r -> (UUID) r[0], r -> (BigDecimal) r[1]));
        return list.stream()
                .map(b -> BudgetResponse.from(b,
                        budgetItemRepository.findByBudgetId(b.getId()),
                        pagoMap.getOrDefault(b.getId(), BigDecimal.ZERO)))
                .toList();
    }

    public BudgetResponse findById(UUID patientId, UUID budgetId) {
        Budget budget = budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        BigDecimal totalPago = budgetPaymentRepository.sumPagoByBudgetId(budgetId);
        return BudgetResponse.from(budget, budgetItemRepository.findByBudgetId(budget.getId()), totalPago);
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
                .descricaoItens(req.descricaoItens())
                .notes(req.notes())
                .validUntil(req.validUntil())
                .status(req.status() != null ? req.status() : Budget.BudgetStatus.EM_ELABORACAO)
                .statusTratamento(req.statusTratamento() != null ? req.statusTratamento() : Budget.TratamentoStatus.NAO_INICIADO)
                .situacaoSaldo(req.situacaoSaldo())
                .declaracaoIr(req.declaracaoIr())
                .dataDeclaracao(req.dataDeclaracao())
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

        BigDecimal total = !items.isEmpty()
                ? items.stream()
                        .map(i -> i.getUnitValue().multiply(BigDecimal.valueOf(i.getQuantity())))
                        .reduce(BigDecimal.ZERO, BigDecimal::add)
                : (req.totalValue() != null ? req.totalValue() : BigDecimal.ZERO);
        saved.setTotalValue(total);
        budgetRepository.save(saved);

        auditService.log(AuditLog.Action.CREATE, "BUDGET", saved.getId(),
                "Orçamento criado para paciente " + patientId, ip);
        return BudgetResponse.from(saved, items, BigDecimal.ZERO);
    }

    @Transactional
    public BudgetResponse update(UUID patientId, UUID budgetId, BudgetRequest req, String ip) {
        Budget budget = budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        AppUser actor = currentUser();

        if (req.dentistId() != null) {
            budget.setDentistId(req.dentistId());
            budget.setDentistName(appUserRepository.findById(req.dentistId()).map(AppUser::getName).orElse(null));
        }
        if (req.descricaoItens() != null) budget.setDescricaoItens(req.descricaoItens());
        if (req.notes() != null) budget.setNotes(req.notes());
        if (req.validUntil() != null) budget.setValidUntil(req.validUntil());
        if (req.totalValue() != null) budget.setTotalValue(req.totalValue());
        if (req.status() != null) budget.setStatus(req.status());
        if (req.statusTratamento() != null) budget.setStatusTratamento(req.statusTratamento());
        budget.setSituacaoSaldo(req.situacaoSaldo());
        budget.setDeclaracaoIr(req.declaracaoIr());
        budget.setDataDeclaracao(req.dataDeclaracao());
        budget.setUpdatedById(actor != null ? actor.getId() : null);
        budget.setUpdatedByName(actor != null ? actor.getName() : null);

        Budget saved = budgetRepository.save(budget);
        BigDecimal totalPago = budgetPaymentRepository.sumPagoByBudgetId(budgetId);
        auditService.log(AuditLog.Action.UPDATE, "BUDGET", saved.getId(),
                "Orçamento atualizado", ip);
        return BudgetResponse.from(saved, budgetItemRepository.findByBudgetId(saved.getId()), totalPago);
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
        BigDecimal totalPago = budgetPaymentRepository.sumPagoByBudgetId(budgetId);
        auditService.log(AuditLog.Action.UPDATE, "BUDGET", saved.getId(),
                "Status alterado para " + status, ip);
        return BudgetResponse.from(saved, budgetItemRepository.findByBudgetId(saved.getId()), totalPago);
    }

    @Transactional
    public void delete(UUID patientId, UUID budgetId, String ip) {
        Budget budget = budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        budgetPaymentRepository.deleteByBudgetId(budgetId);
        budgetItemRepository.deleteByBudgetId(budgetId);
        budgetRepository.delete(budget);
        auditService.log(AuditLog.Action.DELETE, "BUDGET", budgetId,
                "Orçamento removido do paciente " + patientId, ip);
    }

    // ---- Pagamentos ----

    public List<PaymentResponse> listPayments(UUID patientId, UUID budgetId) {
        budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        return budgetPaymentRepository.findByBudgetIdOrderByDataAsc(budgetId).stream()
                .map(PaymentResponse::from).toList();
    }

    @Transactional
    public PaymentResponse addPayment(UUID patientId, UUID budgetId, PaymentRequest req, String ip) {
        budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        BudgetPayment payment = BudgetPayment.builder()
                .budgetId(budgetId)
                .patientId(patientId)
                .data(req.data())
                .valor(req.valor())
                .formaPagamento(req.formaPagamento())
                .status(req.status())
                .build();
        BudgetPayment saved = budgetPaymentRepository.save(payment);
        auditService.log(AuditLog.Action.CREATE, "BUDGET_PAYMENT", saved.getId(),
                "Pagamento registrado para orçamento " + budgetId, ip);
        return PaymentResponse.from(saved);
    }

    @Transactional
    public PaymentResponse togglePaymentStatus(UUID patientId, UUID budgetId, UUID paymentId, String ip) {
        budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        BudgetPayment payment = budgetPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado."));
        payment.setStatus(payment.getStatus() == BudgetPayment.PaymentStatus.PAGO
                ? BudgetPayment.PaymentStatus.PENDENTE
                : BudgetPayment.PaymentStatus.PAGO);
        BudgetPayment saved = budgetPaymentRepository.save(payment);
        auditService.log(AuditLog.Action.UPDATE, "BUDGET_PAYMENT", saved.getId(),
                "Status do pagamento alterado para " + saved.getStatus(), ip);
        return PaymentResponse.from(saved);
    }

    @Transactional
    public void deletePayment(UUID patientId, UUID budgetId, UUID paymentId, String ip) {
        budgetRepository.findByIdAndPatientId(budgetId, patientId)
                .orElseThrow(() -> new NotFoundException("Orçamento não encontrado."));
        BudgetPayment payment = budgetPaymentRepository.findById(paymentId)
                .orElseThrow(() -> new NotFoundException("Pagamento não encontrado."));
        budgetPaymentRepository.delete(payment);
        auditService.log(AuditLog.Action.DELETE, "BUDGET_PAYMENT", paymentId,
                "Pagamento removido do orçamento " + budgetId, ip);
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

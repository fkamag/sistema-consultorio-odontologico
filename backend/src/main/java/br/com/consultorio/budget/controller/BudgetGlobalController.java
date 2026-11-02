package br.com.consultorio.budget.controller;

import br.com.consultorio.budget.dto.BudgetRequest;
import br.com.consultorio.budget.dto.BudgetResponse;
import br.com.consultorio.budget.dto.BudgetSummaryResponse;
import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/budgets")
@RequiredArgsConstructor
public class BudgetGlobalController {

    private final BudgetService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public Page<BudgetSummaryResponse> listAll(
            @RequestParam(required = false) Budget.BudgetStatus status,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.listAll(status, page, size);
    }

    @PostMapping("/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public ResponseEntity<BudgetResponse> create(
            @PathVariable UUID patientId,
            @Valid @RequestBody BudgetRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        BudgetResponse created = service.create(patientId, request, ip);
        return ResponseEntity
                .created(URI.create("/api/patients/" + patientId + "/budgets/" + created.id()))
                .body(created);
    }

    @PutMapping("/{budgetId}/patient/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public BudgetResponse update(
            @PathVariable UUID patientId,
            @PathVariable UUID budgetId,
            @Valid @RequestBody BudgetRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        return service.update(patientId, budgetId, request, ip);
    }
}

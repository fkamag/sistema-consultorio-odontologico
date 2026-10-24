package br.com.consultorio.budget.controller;

import br.com.consultorio.budget.dto.BudgetRequest;
import br.com.consultorio.budget.dto.BudgetResponse;
import br.com.consultorio.budget.entity.Budget;
import br.com.consultorio.budget.service.BudgetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients/{patientId}/budgets")
@RequiredArgsConstructor
public class BudgetController {

    private final BudgetService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<BudgetResponse> list(@PathVariable UUID patientId) {
        return service.listByPatient(patientId);
    }

    @GetMapping("/{budgetId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public BudgetResponse findById(@PathVariable UUID patientId, @PathVariable UUID budgetId) {
        return service.findById(patientId, budgetId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA','SECRETARIA')")
    public ResponseEntity<BudgetResponse> create(
            @PathVariable UUID patientId,
            @Valid @RequestBody BudgetRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        BudgetResponse created = service.create(patientId, request, ip);
        return ResponseEntity
                .created(URI.create("/api/patients/" + patientId + "/budgets/" + created.id()))
                .body(created);
    }

    @PatchMapping("/{budgetId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA','SECRETARIA')")
    public BudgetResponse updateStatus(
            @PathVariable UUID patientId,
            @PathVariable UUID budgetId,
            @RequestParam Budget.BudgetStatus status,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        return service.updateStatus(patientId, budgetId, status, ip);
    }

    @DeleteMapping("/{budgetId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID patientId,
            @PathVariable UUID budgetId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        service.delete(patientId, budgetId, ip);
        return ResponseEntity.noContent().build();
    }
}

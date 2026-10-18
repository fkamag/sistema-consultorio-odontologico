package br.com.consultorio.prontuario.controller;

import br.com.consultorio.prontuario.dto.*;
import br.com.consultorio.prontuario.entity.TreatmentPlanItem;
import br.com.consultorio.prontuario.service.ProntuarioService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes/{patientId}/prontuario")
@RequiredArgsConstructor
public class ProntuarioController {

    private final ProntuarioService service;

    // ── Anamnese ──────────────────────────────────────────────────────────────

    @GetMapping("/anamnese")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<MedicalHistoryResponse> listAnamneses(@PathVariable UUID patientId) {
        return service.listAnamneses(patientId);
    }

    @PostMapping("/anamnese")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public MedicalHistoryResponse createAnamnese(
            @PathVariable UUID patientId,
            @RequestBody @Valid MedicalHistoryRequest req,
            HttpServletRequest http) {
        return service.createAnamnese(patientId, req, http.getRemoteAddr());
    }

    // ── Evolução Clínica ──────────────────────────────────────────────────────

    @GetMapping("/evolucao")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<ClinicalNoteResponse> listNotes(@PathVariable UUID patientId) {
        return service.listNotes(patientId);
    }

    @PostMapping("/evolucao")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public ClinicalNoteResponse createNote(
            @PathVariable UUID patientId,
            @RequestBody @Valid ClinicalNoteRequest req,
            HttpServletRequest http) {
        return service.createNote(patientId, req, http.getRemoteAddr());
    }

    @DeleteMapping("/evolucao/{noteId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA')")
    public void deleteNote(
            @PathVariable UUID patientId,
            @PathVariable UUID noteId,
            HttpServletRequest http) {
        service.deleteNote(patientId, noteId, http.getRemoteAddr());
    }

    // ── Plano de Tratamento ───────────────────────────────────────────────────

    @GetMapping("/plano")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<TreatmentPlanResponse> listPlan(@PathVariable UUID patientId) {
        return service.listPlan(patientId);
    }

    @PostMapping("/plano")
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public TreatmentPlanResponse addPlanItem(
            @PathVariable UUID patientId,
            @RequestBody @Valid TreatmentPlanRequest req,
            HttpServletRequest http) {
        return service.addPlanItem(patientId, req, http.getRemoteAddr());
    }

    @PatchMapping("/plano/{itemId}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public TreatmentPlanResponse updatePlanStatus(
            @PathVariable UUID patientId,
            @PathVariable UUID itemId,
            @RequestParam TreatmentPlanItem.Status status,
            HttpServletRequest http) {
        return service.updatePlanStatus(patientId, itemId, status, http.getRemoteAddr());
    }

    @DeleteMapping("/plano/{itemId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA')")
    public void deletePlanItem(
            @PathVariable UUID patientId,
            @PathVariable UUID itemId,
            HttpServletRequest http) {
        service.deletePlanItem(patientId, itemId, http.getRemoteAddr());
    }
}

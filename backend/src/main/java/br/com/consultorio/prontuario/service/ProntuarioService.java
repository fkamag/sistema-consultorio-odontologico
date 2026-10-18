package br.com.consultorio.prontuario.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.patient.entity.Patient;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.prontuario.dto.*;
import br.com.consultorio.prontuario.entity.ClinicalNote;
import br.com.consultorio.prontuario.entity.MedicalHistory;
import br.com.consultorio.prontuario.entity.TreatmentPlanItem;
import br.com.consultorio.prontuario.repository.ClinicalNoteRepository;
import br.com.consultorio.prontuario.repository.MedicalHistoryRepository;
import br.com.consultorio.prontuario.repository.TreatmentPlanRepository;
import br.com.consultorio.shared.exception.NotFoundException;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ProntuarioService {

    private final MedicalHistoryRepository medicalHistoryRepository;
    private final ClinicalNoteRepository   clinicalNoteRepository;
    private final TreatmentPlanRepository  treatmentPlanRepository;
    private final PatientRepository        patientRepository;
    private final AppUserRepository        userRepository;
    private final AuditService             auditService;

    // ── Anamnese ──────────────────────────────────────────────────────────────

    public List<MedicalHistoryResponse> listAnamneses(UUID patientId) {
        requirePatient(patientId);
        return medicalHistoryRepository.findByPatientIdOrderByVersionNumberDesc(patientId)
                .stream().map(MedicalHistoryResponse::from).toList();
    }

    @Transactional
    public MedicalHistoryResponse createAnamnese(UUID patientId, MedicalHistoryRequest req, String ip) {
        Patient patient = requirePatient(patientId);
        int nextVersion = medicalHistoryRepository
                .findTopByPatientIdOrderByVersionNumberDesc(patientId)
                .map(h -> h.getVersionNumber() + 1).orElse(1);

        AppUser actor = currentUser();
        MedicalHistory h = MedicalHistory.builder()
                .patientId(patientId)
                .versionNumber(nextVersion)
                .alergias(req.alergias())
                .medicamentos(req.medicamentos())
                .diabetes(req.diabetes())
                .hipertensao(req.hipertensao())
                .problemasCardiacos(req.problemasCardiacos())
                .tratamentosMedicos(req.tratamentosMedicos())
                .cirurgias(req.cirurgias())
                .observacoes(req.observacoes())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .build();

        MedicalHistory saved = medicalHistoryRepository.save(h);
        auditService.log(AuditLog.Action.CREATE, "MEDICAL_HISTORY", saved.getId(),
                "Anamnese v" + nextVersion + " registrada para " + patient.getName(), ip);
        return MedicalHistoryResponse.from(saved);
    }

    // ── Evolução Clínica ──────────────────────────────────────────────────────

    public List<ClinicalNoteResponse> listNotes(UUID patientId) {
        requirePatient(patientId);
        return clinicalNoteRepository.findByPatientIdOrderByNoteDateDescCreatedAtDesc(patientId)
                .stream().map(n -> {
                    String dentistName = n.getDentistId() != null
                            ? userRepository.findById(n.getDentistId()).map(AppUser::getName).orElse("")
                            : "";
                    return ClinicalNoteResponse.from(n, dentistName);
                }).toList();
    }

    @Transactional
    public ClinicalNoteResponse createNote(UUID patientId, ClinicalNoteRequest req, String ip) {
        Patient patient = requirePatient(patientId);
        AppUser actor = currentUser();

        ClinicalNote note = ClinicalNote.builder()
                .patientId(patientId)
                .dentistId(actor != null ? actor.getId() : null)
                .appointmentId(req.appointmentId())
                .noteDate(req.noteDate())
                .noteType(req.noteType())
                .tooth(req.tooth())
                .description(req.description())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .build();

        ClinicalNote saved = clinicalNoteRepository.save(note);
        auditService.log(AuditLog.Action.CREATE, "CLINICAL_NOTE", saved.getId(),
                "Evolução clínica registrada para " + patient.getName(), ip);

        String dentistName = actor != null ? actor.getName() : "";
        return ClinicalNoteResponse.from(saved, dentistName);
    }

    @Transactional
    public void deleteNote(UUID patientId, UUID noteId, String ip) {
        ClinicalNote note = clinicalNoteRepository.findById(noteId)
                .filter(n -> n.getPatientId().equals(patientId))
                .orElseThrow(() -> new NotFoundException("Evolução não encontrada."));
        clinicalNoteRepository.delete(note);
        auditService.log(AuditLog.Action.DELETE, "CLINICAL_NOTE", noteId,
                "Evolução clínica removida", ip);
    }

    // ── Plano de Tratamento ───────────────────────────────────────────────────

    public List<TreatmentPlanResponse> listPlan(UUID patientId) {
        requirePatient(patientId);
        return treatmentPlanRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(TreatmentPlanResponse::from).toList();
    }

    @Transactional
    public TreatmentPlanResponse addPlanItem(UUID patientId, TreatmentPlanRequest req, String ip) {
        Patient patient = requirePatient(patientId);
        AppUser actor = currentUser();

        TreatmentPlanItem item = TreatmentPlanItem.builder()
                .patientId(patientId)
                .description(req.description())
                .tooth(req.tooth())
                .valueEstimated(req.valueEstimated())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .updatedById(actor != null ? actor.getId() : null)
                .updatedByName(actor != null ? actor.getName() : null)
                .build();

        TreatmentPlanItem saved = treatmentPlanRepository.save(item);
        auditService.log(AuditLog.Action.CREATE, "TREATMENT_PLAN", saved.getId(),
                "Item de plano adicionado para " + patient.getName() + ": " + req.description(), ip);
        return TreatmentPlanResponse.from(saved);
    }

    @Transactional
    public TreatmentPlanResponse updatePlanStatus(UUID patientId, UUID itemId,
                                                   TreatmentPlanItem.Status status, String ip) {
        TreatmentPlanItem item = treatmentPlanRepository.findById(itemId)
                .filter(i -> i.getPatientId().equals(patientId))
                .orElseThrow(() -> new NotFoundException("Item do plano não encontrado."));

        AppUser actor = currentUser();
        item.setStatus(status);
        item.setUpdatedAt(LocalDateTime.now());
        item.setUpdatedById(actor != null ? actor.getId() : null);
        item.setUpdatedByName(actor != null ? actor.getName() : null);

        TreatmentPlanItem saved = treatmentPlanRepository.save(item);
        auditService.log(AuditLog.Action.UPDATE, "TREATMENT_PLAN", itemId,
                "Status alterado para " + status, ip);
        return TreatmentPlanResponse.from(saved);
    }

    @Transactional
    public void deletePlanItem(UUID patientId, UUID itemId, String ip) {
        TreatmentPlanItem item = treatmentPlanRepository.findById(itemId)
                .filter(i -> i.getPatientId().equals(patientId))
                .orElseThrow(() -> new NotFoundException("Item do plano não encontrado."));
        treatmentPlanRepository.delete(item);
        auditService.log(AuditLog.Action.DELETE, "TREATMENT_PLAN", itemId,
                "Item do plano removido: " + item.getDescription(), ip);
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Patient requirePatient(UUID patientId) {
        return patientRepository.findByIdAndDeletedAtIsNull(patientId)
                .orElseThrow(() -> new NotFoundException("Paciente não encontrado."));
    }

    private AppUser currentUser() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        return userRepository.findByEmail(auth.getName()).orElse(null);
    }
}

package br.com.consultorio.prontuario;

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
import br.com.consultorio.prontuario.service.ProntuarioService;
import br.com.consultorio.shared.exception.NotFoundException;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ProntuarioServiceTest {

    @Mock private MedicalHistoryRepository medicalHistoryRepository;
    @Mock private ClinicalNoteRepository   clinicalNoteRepository;
    @Mock private TreatmentPlanRepository  treatmentPlanRepository;
    @Mock private PatientRepository        patientRepository;
    @Mock private AppUserRepository        userRepository;
    @Mock private AuditService             auditService;

    @InjectMocks private ProntuarioService service;

    private final UUID patientId = UUID.randomUUID();
    private final UUID dentistId = UUID.randomUUID();

    @BeforeEach
    void setUp() {
        var auth = new UsernamePasswordAuthenticationToken("dentista@consultorio.com", null, List.of());
        SecurityContextHolder.getContext().setAuthentication(auth);
        when(userRepository.findByEmail("dentista@consultorio.com")).thenReturn(Optional.of(dentista()));
    }

    @AfterEach
    void tearDown() { SecurityContextHolder.clearContext(); }

    // ── helpers ───────────────────────────────────────────────────────────────

    private Patient paciente() {
        Patient p = new Patient();
        ReflectionTestUtils.setField(p, "id", patientId);
        ReflectionTestUtils.setField(p, "name", "João Silva");
        return p;
    }

    private AppUser dentista() {
        AppUser u = new AppUser();
        ReflectionTestUtils.setField(u, "id", dentistId);
        ReflectionTestUtils.setField(u, "name", "Dr. Valdomiro");
        ReflectionTestUtils.setField(u, "email", "dentista@consultorio.com");
        return u;
    }

    private MedicalHistory anamnese(int version) {
        MedicalHistory h = new MedicalHistory();
        ReflectionTestUtils.setField(h, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(h, "patientId", patientId);
        ReflectionTestUtils.setField(h, "versionNumber", version);
        ReflectionTestUtils.setField(h, "alergias", "Penicilina");
        ReflectionTestUtils.setField(h, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(h, "createdByName", "Dr. Valdomiro");
        return h;
    }

    private ClinicalNote nota() {
        ClinicalNote n = new ClinicalNote();
        ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(n, "patientId", patientId);
        ReflectionTestUtils.setField(n, "dentistId", dentistId);
        ReflectionTestUtils.setField(n, "noteDate", LocalDate.now());
        ReflectionTestUtils.setField(n, "noteType", ClinicalNote.NoteType.CONSULTA);
        ReflectionTestUtils.setField(n, "description", "Limpeza realizada.");
        ReflectionTestUtils.setField(n, "createdAt", LocalDateTime.now());
        return n;
    }

    private TreatmentPlanItem itemPlano() {
        TreatmentPlanItem i = new TreatmentPlanItem();
        ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(i, "patientId", patientId);
        ReflectionTestUtils.setField(i, "description", "Restauração dente 26");
        ReflectionTestUtils.setField(i, "status", TreatmentPlanItem.Status.PLANEJADO);
        ReflectionTestUtils.setField(i, "createdAt", LocalDateTime.now());
        ReflectionTestUtils.setField(i, "updatedAt", LocalDateTime.now());
        return i;
    }

    // ── Anamnese ──────────────────────────────────────────────────────────────

    @Test
    void listAnamnesesDeveRetornarTodasAsVersoes() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(medicalHistoryRepository.findByPatientIdOrderByVersionNumberDesc(patientId))
                .thenReturn(List.of(anamnese(2), anamnese(1)));

        List<MedicalHistoryResponse> result = service.listAnamneses(patientId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).versionNumber()).isEqualTo(2);
    }

    @Test
    void createAnamneseDeveIncrementarVersao() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(medicalHistoryRepository.findTopByPatientIdOrderByVersionNumberDesc(patientId))
                .thenReturn(Optional.of(anamnese(1)));
        when(medicalHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistoryRequest req = new MedicalHistoryRequest("Penicilina", "", false, false, false, "", "", "");
        MedicalHistoryResponse result = service.createAnamnese(patientId, req, "127.0.0.1");

        assertThat(result.versionNumber()).isEqualTo(2);
        verify(auditService).log(any(), eq("MEDICAL_HISTORY"), any(), any(), any());
    }

    @Test
    void createAnamnesePrimeiraVersaoDeveSerUm() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(medicalHistoryRepository.findTopByPatientIdOrderByVersionNumberDesc(patientId))
                .thenReturn(Optional.empty());
        when(medicalHistoryRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        MedicalHistoryRequest req = new MedicalHistoryRequest(null, null, false, true, false, null, null, null);
        MedicalHistoryResponse result = service.createAnamnese(patientId, req, "127.0.0.1");

        assertThat(result.versionNumber()).isEqualTo(1);
        assertThat(result.hipertensao()).isTrue();
    }

    @Test
    void listAnamnesesDeveLancarNotFoundParaPacienteInexistente() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.listAnamneses(patientId))
                .isInstanceOf(NotFoundException.class);
    }

    // ── Evolução Clínica ──────────────────────────────────────────────────────

    @Test
    void listNotesDeveRetornarEvolucoes() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(clinicalNoteRepository.findByPatientIdOrderByNoteDateDescCreatedAtDesc(patientId))
                .thenReturn(List.of(nota()));
        when(userRepository.findById(dentistId)).thenReturn(Optional.of(dentista()));

        List<ClinicalNoteResponse> result = service.listNotes(patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dentistName()).isEqualTo("Dr. Valdomiro");
    }

    @Test
    void createNoteDeveSalvarEAuditar() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(clinicalNoteRepository.save(any())).thenAnswer(inv -> {
            ClinicalNote n = inv.getArgument(0);
            ReflectionTestUtils.setField(n, "id", UUID.randomUUID());
            return n;
        });

        ClinicalNoteRequest req = new ClinicalNoteRequest(
                LocalDate.now(), ClinicalNote.NoteType.PROCEDIMENTO, "26", "Restauração realizada.", null);
        ClinicalNoteResponse result = service.createNote(patientId, req, "127.0.0.1");

        assertThat(result.tooth()).isEqualTo("26");
        assertThat(result.noteType()).isEqualTo(ClinicalNote.NoteType.PROCEDIMENTO);
        verify(auditService).log(any(), eq("CLINICAL_NOTE"), any(), any(), any());
    }

    @Test
    void deleteNoteDeveLancarNotFoundParaNoteDeOutroPaciente() {
        UUID outroPaciente = UUID.randomUUID();
        UUID noteId = UUID.randomUUID();
        ClinicalNote n = nota();
        ReflectionTestUtils.setField(n, "id", noteId);

        when(clinicalNoteRepository.findById(noteId)).thenReturn(Optional.of(n));

        assertThatThrownBy(() -> service.deleteNote(outroPaciente, noteId, "127.0.0.1"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── Plano de Tratamento ───────────────────────────────────────────────────

    @Test
    void listPlanDeveRetornarItens() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(treatmentPlanRepository.findByPatientIdOrderByCreatedAtDesc(patientId))
                .thenReturn(List.of(itemPlano()));

        List<TreatmentPlanResponse> result = service.listPlan(patientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).status()).isEqualTo(TreatmentPlanItem.Status.PLANEJADO);
    }

    @Test
    void addPlanItemDeveSalvarComStatusPlanejado() {
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente()));
        when(treatmentPlanRepository.save(any())).thenAnswer(inv -> {
            TreatmentPlanItem i = inv.getArgument(0);
            ReflectionTestUtils.setField(i, "id", UUID.randomUUID());
            return i;
        });

        TreatmentPlanRequest req = new TreatmentPlanRequest("Canal dente 36", "36", new BigDecimal("800.00"));
        TreatmentPlanResponse result = service.addPlanItem(patientId, req, "127.0.0.1");

        assertThat(result.description()).isEqualTo("Canal dente 36");
        assertThat(result.status()).isEqualTo(TreatmentPlanItem.Status.PLANEJADO);
        assertThat(result.valueEstimated()).isEqualByComparingTo("800.00");
    }

    @Test
    void updatePlanStatusDeveAlterarStatus() {
        UUID itemId = UUID.randomUUID();
        TreatmentPlanItem item = itemPlano();
        ReflectionTestUtils.setField(item, "id", itemId);

        when(treatmentPlanRepository.findById(itemId)).thenReturn(Optional.of(item));
        when(treatmentPlanRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        TreatmentPlanResponse result = service.updatePlanStatus(
                patientId, itemId, TreatmentPlanItem.Status.APROVADO, "127.0.0.1");

        assertThat(result.status()).isEqualTo(TreatmentPlanItem.Status.APROVADO);
        verify(auditService).log(any(), eq("TREATMENT_PLAN"), any(), any(), any());
    }

    @Test
    void updatePlanStatusDeveLancarNotFoundParaItemDeOutroPaciente() {
        UUID outroPaciente = UUID.randomUUID();
        UUID itemId = UUID.randomUUID();
        TreatmentPlanItem item = itemPlano();
        ReflectionTestUtils.setField(item, "id", itemId);

        when(treatmentPlanRepository.findById(itemId)).thenReturn(Optional.of(item));

        assertThatThrownBy(() -> service.updatePlanStatus(
                outroPaciente, itemId, TreatmentPlanItem.Status.REALIZADO, "127.0.0.1"))
                .isInstanceOf(NotFoundException.class);
    }
}

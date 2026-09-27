package br.com.consultorio.patient;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.patient.dto.PatientRequest;
import br.com.consultorio.patient.dto.PatientResponse;
import br.com.consultorio.patient.entity.Patient;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.patient.service.PatientService;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientServiceTest {

    @Mock private PatientRepository repository;
    @Mock private AuditService auditService;

    @InjectMocks private PatientService service;

    private AppUser adminUser;

    @BeforeEach
    void setUp() {
        adminUser = new AppUser();
        ReflectionTestUtils.setField(adminUser, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(adminUser, "name", "Admin Teste");
        ReflectionTestUtils.setField(adminUser, "email", "admin@consultorio.com");
        ReflectionTestUtils.setField(adminUser, "role", AppUser.Role.ADMIN);
        ReflectionTestUtils.setField(adminUser, "active", true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private PatientRequest requestValido() {
        return new PatientRequest(
                "João da Silva", "123.456.789-09", LocalDate.of(1990, 5, 20),
                "(11) 98765-4321", "joao@email.com",
                "Rua das Flores", "100", "Apto 2", "Centro", "São Paulo", "SP", null
        );
    }

    private Patient pacienteSalvo(UUID id, String nome) {
        Patient p = new Patient();
        ReflectionTestUtils.setField(p, "id", id);
        ReflectionTestUtils.setField(p, "name", nome);
        ReflectionTestUtils.setField(p, "cpf", "123.456.789-09");
        ReflectionTestUtils.setField(p, "active", true);
        ReflectionTestUtils.setField(p, "createdByName", "Admin Teste");
        ReflectionTestUtils.setField(p, "updatedByName", "Admin Teste");
        return p;
    }

    // ── list ──────────────────────────────────────────────────────────────────

    @Test
    void listDeveRetornarPaginaDeRespostas() {
        UUID id = UUID.randomUUID();
        Patient paciente = pacienteSalvo(id, "João da Silva");
        when(repository.search(anyString(), any())).thenReturn(new PageImpl<>(List.of(paciente)));

        var resultado = service.list("João", 0, 10);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).name()).isEqualTo("João da Silva");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findByIdDeveRetornarPacienteQuandoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id))
                .thenReturn(Optional.of(pacienteSalvo(id, "Maria")));

        PatientResponse response = service.findById(id);

        assertThat(response.name()).isEqualTo("Maria");
    }

    @Test
    void findByIdDeveLancarNotFoundQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Paciente não encontrado.");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void createDeveSalvarERegistrarAuditoria() {
        PatientRequest req = requestValido();
        UUID id = UUID.randomUUID();
        Patient salvo = pacienteSalvo(id, req.name());

        when(repository.existsByCpfAndDeletedAtIsNull(req.cpf())).thenReturn(false);
        when(repository.save(any())).thenReturn(salvo);

        PatientResponse response = service.create(req, "127.0.0.1");

        assertThat(response.name()).isEqualTo(req.name());
        assertThat(response.createdByName()).isEqualTo("Admin Teste");
        verify(auditService).log(eq(AuditLog.Action.CREATE), eq("PATIENT"), eq(id), anyString(), eq("127.0.0.1"));
    }

    @Test
    void createDeveLancarConflictQuandoCpfJaCadastrado() {
        PatientRequest req = requestValido();
        when(repository.existsByCpfAndDeletedAtIsNull(req.cpf())).thenReturn(true);

        assertThatThrownBy(() -> service.create(req, "127.0.0.1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("CPF");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void updateDeveAlterarDadosERegistrarAuditoria() {
        UUID id = UUID.randomUUID();
        PatientRequest req = requestValido();
        Patient existente = pacienteSalvo(id, "Nome Antigo");

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(existente));
        when(repository.existsByCpfAndIdNotAndDeletedAtIsNull(req.cpf(), id)).thenReturn(false);
        when(repository.save(any())).thenReturn(existente);

        service.update(id, req, "10.0.0.1");

        verify(auditService).log(eq(AuditLog.Action.UPDATE), eq("PATIENT"), eq(id), anyString(), eq("10.0.0.1"));
        assertThat(existente.getUpdatedByName()).isEqualTo("Admin Teste");
    }

    @Test
    void updateDeveLancarConflictQuandoCpfPertenceAOutroPaciente() {
        UUID id = UUID.randomUUID();
        PatientRequest req = requestValido();

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(pacienteSalvo(id, "Ana")));
        when(repository.existsByCpfAndIdNotAndDeletedAtIsNull(req.cpf(), id)).thenReturn(true);

        assertThatThrownBy(() -> service.update(id, req, "127.0.0.1"))
                .isInstanceOf(ConflictException.class);
    }

    // ── delete ────────────────────────────────────────────────────────────────

    @Test
    void deleteDeveFazerSoftDeleteERegistrarAuditoria() {
        UUID id = UUID.randomUUID();
        Patient paciente = pacienteSalvo(id, "Carlos");

        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(paciente));
        when(repository.save(any())).thenReturn(paciente);

        service.delete(id, "192.168.0.1");

        assertThat(paciente.getDeletedAt()).isNotNull();
        assertThat(paciente.isActive()).isFalse();
        verify(auditService).log(eq(AuditLog.Action.DELETE), eq("PATIENT"), eq(id), anyString(), eq("192.168.0.1"));
    }

    @Test
    void deleteDeveLancarNotFoundQuandoPacienteNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.delete(id, "127.0.0.1"))
                .isInstanceOf(NotFoundException.class);
    }
}

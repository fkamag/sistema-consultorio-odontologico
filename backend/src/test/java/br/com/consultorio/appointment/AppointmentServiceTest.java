package br.com.consultorio.appointment;

import br.com.consultorio.appointment.dto.AppointmentRequest;
import br.com.consultorio.appointment.dto.AppointmentResponse;
import br.com.consultorio.appointment.entity.Appointment;
import br.com.consultorio.appointment.repository.AppointmentRepository;
import br.com.consultorio.appointment.service.AppointmentService;
import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.patient.entity.Patient;
import br.com.consultorio.patient.repository.PatientRepository;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock private AppointmentRepository repository;
    @Mock private PatientRepository patientRepository;
    @Mock private AppUserRepository userRepository;
    @Mock private AuditService auditService;

    @InjectMocks private AppointmentService service;

    private AppUser adminUser;
    private UUID patientId;
    private UUID dentistId;
    private UUID appointmentId;

    @BeforeEach
    void setUp() {
        patientId     = UUID.randomUUID();
        dentistId     = UUID.randomUUID();
        appointmentId = UUID.randomUUID();

        adminUser = new AppUser();
        ReflectionTestUtils.setField(adminUser, "id",    UUID.randomUUID());
        ReflectionTestUtils.setField(adminUser, "name",  "Admin Teste");
        ReflectionTestUtils.setField(adminUser, "email", "admin@consultorio.com");
        ReflectionTestUtils.setField(adminUser, "role",  AppUser.Role.ADMIN);
        ReflectionTestUtils.setField(adminUser, "active", true);

        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminUser, null, adminUser.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private AppointmentRequest requestValido(LocalDateTime inicio) {
        return new AppointmentRequest(patientId, dentistId, inicio, 30, "Revisão");
    }

    private Appointment agendamentoSalvo(UUID id, LocalDateTime inicio, Appointment.Status status) {
        Appointment a = new Appointment();
        ReflectionTestUtils.setField(a, "id",           id);
        ReflectionTestUtils.setField(a, "patientId",    patientId);
        ReflectionTestUtils.setField(a, "dentistId",    dentistId);
        ReflectionTestUtils.setField(a, "startTime",    inicio);
        ReflectionTestUtils.setField(a, "durationMinutes", 30);
        ReflectionTestUtils.setField(a, "status",       status);
        ReflectionTestUtils.setField(a, "createdByName", "Admin Teste");
        ReflectionTestUtils.setField(a, "updatedByName", "Admin Teste");
        ReflectionTestUtils.setField(a, "createdAt",    LocalDateTime.now());
        ReflectionTestUtils.setField(a, "updatedAt",    LocalDateTime.now());
        return a;
    }

    private Patient paciente(UUID id) {
        Patient p = new Patient();
        ReflectionTestUtils.setField(p, "id",   id);
        ReflectionTestUtils.setField(p, "name", "João da Silva");
        return p;
    }

    private AppUser dentista(UUID id) {
        AppUser u = new AppUser();
        ReflectionTestUtils.setField(u, "id",   id);
        ReflectionTestUtils.setField(u, "name", "Dr. Valdomiro");
        ReflectionTestUtils.setField(u, "role", AppUser.Role.DENTISTA);
        ReflectionTestUtils.setField(u, "active", true);
        return u;
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void createDeveSalvarERegistrarAuditoria() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        AppointmentRequest req = requestValido(inicio);

        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.of(paciente(patientId)));
        when(userRepository.findById(dentistId))
                .thenReturn(Optional.of(dentista(dentistId)));
        when(repository.existsConflict(eq(dentistId), eq(inicio), any(), isNull()))
                .thenReturn(false);

        Appointment salvo = agendamentoSalvo(appointmentId, inicio, Appointment.Status.AGENDADO);
        when(repository.save(any())).thenReturn(salvo);

        AppointmentResponse response = service.create(req, "127.0.0.1");

        assertThat(response.id()).isEqualTo(appointmentId);
        assertThat(response.status()).isEqualTo("AGENDADO");
        verify(auditService).log(eq(AuditLog.Action.CREATE), eq("APPOINTMENT"), eq(appointmentId), anyString(), eq("127.0.0.1"));
    }

    @Test
    void createDeveLancarConflictQuandoHorarioOcupado() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1).withHour(9).withMinute(0);
        AppointmentRequest req = requestValido(inicio);

        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.of(paciente(patientId)));
        when(userRepository.findById(dentistId))
                .thenReturn(Optional.of(dentista(dentistId)));
        when(repository.existsConflict(eq(dentistId), eq(inicio), any(), isNull()))
                .thenReturn(true);

        assertThatThrownBy(() -> service.create(req, "127.0.0.1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("horário");
    }

    @Test
    void createDeveLancarNotFoundQuandoPacienteInexistente() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        AppointmentRequest req = requestValido(inicio);

        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req, "127.0.0.1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Paciente");
    }

    @Test
    void createDeveLancarNotFoundQuandoDentistaInexistente() {
        LocalDateTime inicio = LocalDateTime.now().plusDays(1);
        AppointmentRequest req = requestValido(inicio);

        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.of(paciente(patientId)));
        when(userRepository.findById(dentistId))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.create(req, "127.0.0.1"))
                .isInstanceOf(NotFoundException.class)
                .hasMessageContaining("Dentista");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void updateDeveAlterarHorarioERegistrarAuditoria() {
        LocalDateTime novoInicio = LocalDateTime.now().plusDays(2).withHour(14).withMinute(0);
        AppointmentRequest req = requestValido(novoInicio);
        Appointment existente = agendamentoSalvo(appointmentId, LocalDateTime.now().plusDays(1), Appointment.Status.AGENDADO);

        when(repository.findById(appointmentId)).thenReturn(Optional.of(existente));
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.of(paciente(patientId)));
        when(userRepository.findById(dentistId))
                .thenReturn(Optional.of(dentista(dentistId)));
        when(repository.existsConflict(eq(dentistId), eq(novoInicio), any(), eq(appointmentId)))
                .thenReturn(false);
        when(repository.save(any())).thenReturn(existente);

        service.update(appointmentId, req, "127.0.0.1");

        verify(auditService).log(eq(AuditLog.Action.UPDATE), eq("APPOINTMENT"), eq(appointmentId), anyString(), anyString());
    }

    @Test
    void updateDeveLancarNotFoundQuandoAgendamentoInexistente() {
        when(repository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.update(appointmentId, requestValido(LocalDateTime.now().plusDays(1)), "ip"))
                .isInstanceOf(NotFoundException.class);
    }

    // ── changeStatus ──────────────────────────────────────────────────────────

    @Test
    void changeStatusDeveAtualizarStatusParaConfirmado() {
        Appointment existente = agendamentoSalvo(appointmentId, LocalDateTime.now().plusDays(1), Appointment.Status.AGENDADO);

        when(repository.findById(appointmentId)).thenReturn(Optional.of(existente));
        when(userRepository.findById(dentistId)).thenReturn(Optional.of(dentista(dentistId)));
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId)).thenReturn(Optional.of(paciente(patientId)));
        when(repository.save(any())).thenReturn(existente);

        AppointmentResponse response = service.changeStatus(appointmentId, Appointment.Status.CONFIRMADO, "127.0.0.1");

        assertThat(existente.getStatus()).isEqualTo(Appointment.Status.CONFIRMADO);
        assertThat(response.status()).isEqualTo("CONFIRMADO");
    }

    @Test
    void changeStatusDeveLancarConflictQuandoStatusInvalido() {
        Appointment realizado = agendamentoSalvo(appointmentId, LocalDateTime.now().minusDays(1), Appointment.Status.REALIZADO);
        when(repository.findById(appointmentId)).thenReturn(Optional.of(realizado));

        assertThatThrownBy(() -> service.changeStatus(appointmentId, Appointment.Status.AGENDADO, "ip"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("REALIZADO");
    }

    // ── findByPeriod ──────────────────────────────────────────────────────────

    @Test
    void findByPeriodComDentistaDeveRetornarListaOrdenada() {
        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime fim    = inicio.plusDays(7);
        Appointment a = agendamentoSalvo(appointmentId, inicio.plusHours(9), Appointment.Status.AGENDADO);

        when(repository.findByDentistIdAndStartTimeBetweenOrderByStartTime(dentistId, inicio, fim))
                .thenReturn(List.of(a));
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.of(paciente(patientId)));
        when(userRepository.findById(dentistId))
                .thenReturn(Optional.of(dentista(dentistId)));

        List<AppointmentResponse> result = service.findByPeriod(dentistId, inicio, fim);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).dentistName()).isEqualTo("Dr. Valdomiro");
    }

    @Test
    void findByPeriodSemDentistaDeveRetornarTodos() {
        LocalDateTime inicio = LocalDateTime.now().withHour(0).withMinute(0);
        LocalDateTime fim    = inicio.plusDays(7);
        Appointment a = agendamentoSalvo(appointmentId, inicio.plusHours(9), Appointment.Status.AGENDADO);

        when(repository.findByStartTimeBetweenOrderByStartTime(inicio, fim))
                .thenReturn(List.of(a));
        when(patientRepository.findByIdAndDeletedAtIsNull(patientId))
                .thenReturn(Optional.of(paciente(patientId)));
        when(userRepository.findById(dentistId))
                .thenReturn(Optional.of(dentista(dentistId)));

        List<AppointmentResponse> result = service.findByPeriod(null, inicio, fim);

        assertThat(result).hasSize(1);
    }
}

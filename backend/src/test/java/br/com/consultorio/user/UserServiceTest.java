package br.com.consultorio.user;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import br.com.consultorio.user.dto.UserRequest;
import br.com.consultorio.user.dto.UserResponse;
import br.com.consultorio.user.service.UserService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock private AppUserRepository repository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private AuditService auditService;

    @InjectMocks private UserService service;

    private AppUser adminLogado;

    @BeforeEach
    void setUp() {
        adminLogado = usuario(UUID.randomUUID(), "Admin", "admin@consultorio.com", AppUser.Role.ADMIN);
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(adminLogado, null, adminLogado.getAuthorities())
        );
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AppUser usuario(UUID id, String nome, String email, AppUser.Role role) {
        AppUser u = new AppUser();
        ReflectionTestUtils.setField(u, "id", id);
        ReflectionTestUtils.setField(u, "name", nome);
        ReflectionTestUtils.setField(u, "email", email);
        ReflectionTestUtils.setField(u, "passwordHash", "$2a$12$hash");
        ReflectionTestUtils.setField(u, "role", role);
        ReflectionTestUtils.setField(u, "active", true);
        return u;
    }

    private UserRequest requestValido(String email, String role) {
        return new UserRequest("Dr. Novo", email, "Senha@123", role);
    }

    // ── list ──────────────────────────────────────────────────────────────────

    @Test
    void listDeveRetornarPaginaDeUsuarios() {
        AppUser u = usuario(UUID.randomUUID(), "Ana", "ana@consultorio.com", AppUser.Role.SECRETARIA);
        when(repository.search(anyString(), any())).thenReturn(new PageImpl<>(List.of(u)));

        var resultado = service.list("Ana", 0, 10);

        assertThat(resultado.getContent()).hasSize(1);
        assertThat(resultado.getContent().get(0).name()).isEqualTo("Ana");
    }

    // ── findById ──────────────────────────────────────────────────────────────

    @Test
    void findByIdDeveRetornarUsuarioQuandoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id))
                .thenReturn(Optional.of(usuario(id, "Carlos", "carlos@c.com", AppUser.Role.DENTISTA)));

        UserResponse resp = service.findById(id);

        assertThat(resp.name()).isEqualTo("Carlos");
        assertThat(resp.role()).isEqualTo("DENTISTA");
    }

    @Test
    void findByIdDeveLancarNotFoundQuandoNaoExiste() {
        UUID id = UUID.randomUUID();
        when(repository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.findById(id))
                .isInstanceOf(NotFoundException.class)
                .hasMessage("Usuário não encontrado.");
    }

    // ── create ────────────────────────────────────────────────────────────────

    @Test
    void createDeveSalvarUsuarioComSenhaCriptografadaERegistrarAuditoria() {
        UserRequest req = requestValido("novo@consultorio.com", "SECRETARIA");
        UUID novoId = UUID.randomUUID();
        AppUser salvo = usuario(novoId, req.name(), req.email(), AppUser.Role.SECRETARIA);

        when(repository.existsByEmail(req.email())).thenReturn(false);
        when(passwordEncoder.encode(req.password())).thenReturn("$2a$12$encoded");
        when(repository.save(any())).thenReturn(salvo);

        UserResponse resp = service.create(req, "127.0.0.1");

        assertThat(resp.email()).isEqualTo("novo@consultorio.com");
        assertThat(resp.role()).isEqualTo("SECRETARIA");

        ArgumentCaptor<AppUser> captor = ArgumentCaptor.forClass(AppUser.class);
        verify(repository).save(captor.capture());
        assertThat(captor.getValue().getPassword()).isEqualTo("$2a$12$encoded");

        verify(auditService).log(eq(AuditLog.Action.CREATE), eq("USER"), eq(novoId), anyString(), eq("127.0.0.1"));
    }

    @Test
    void createDeveLancarConflictQuandoEmailJaCadastrado() {
        UserRequest req = requestValido("admin@consultorio.com", "ADMIN");
        when(repository.existsByEmail(req.email())).thenReturn(true);

        assertThatThrownBy(() -> service.create(req, "127.0.0.1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("e-mail");
    }

    // ── update ────────────────────────────────────────────────────────────────

    @Test
    void updateDeveAlterarDadosSemTrocarSenhaQuandoSenhaNula() {
        UUID id = UUID.randomUUID();
        AppUser existente = usuario(id, "Antes", "antes@c.com", AppUser.Role.DENTISTA);
        UserRequest req = new UserRequest("Depois", "antes@c.com", null, "DENTISTA");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot(req.email(), id)).thenReturn(false);
        when(repository.save(any())).thenReturn(existente);

        service.update(id, req, "10.0.0.1");

        verify(passwordEncoder, never()).encode(any());
        verify(auditService).log(eq(AuditLog.Action.UPDATE), eq("USER"), eq(id), anyString(), eq("10.0.0.1"));
    }

    @Test
    void updateDeveTrocarSenhaQuandoFornecida() {
        UUID id = UUID.randomUUID();
        AppUser existente = usuario(id, "Ana", "ana@c.com", AppUser.Role.SECRETARIA);
        UserRequest req = new UserRequest("Ana", "ana@c.com", "NovaSenha@1", "SECRETARIA");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot(req.email(), id)).thenReturn(false);
        when(passwordEncoder.encode("NovaSenha@1")).thenReturn("$2a$12$novo");
        when(repository.save(any())).thenReturn(existente);

        service.update(id, req, "10.0.0.1");

        verify(passwordEncoder).encode("NovaSenha@1");
        assertThat(existente.getPasswordHash()).isEqualTo("$2a$12$novo");
    }

    @Test
    void updateDeveLancarConflictQuandoEmailPertenceAOutroUsuario() {
        UUID id = UUID.randomUUID();
        AppUser existente = usuario(id, "Ana", "ana@c.com", AppUser.Role.SECRETARIA);
        UserRequest req = new UserRequest("Ana", "outro@c.com", null, "SECRETARIA");

        when(repository.findById(id)).thenReturn(Optional.of(existente));
        when(repository.existsByEmailAndIdNot(req.email(), id)).thenReturn(true);

        assertThatThrownBy(() -> service.update(id, req, "127.0.0.1"))
                .isInstanceOf(ConflictException.class);
    }

    // ── toggleActive ──────────────────────────────────────────────────────────

    @Test
    void toggleActiveDeveDesativarUsuarioAtivoERegistrarAuditoria() {
        UUID id = UUID.randomUUID();
        AppUser ativo = usuario(id, "Pedro", "pedro@c.com", AppUser.Role.DENTISTA);
        when(repository.findById(id)).thenReturn(Optional.of(ativo));
        when(repository.save(any())).thenReturn(ativo);

        service.toggleActive(id, "127.0.0.1");

        assertThat(ativo.isActive()).isFalse();
        verify(auditService).log(eq(AuditLog.Action.UPDATE), eq("USER"), eq(id), anyString(), eq("127.0.0.1"));
    }

    @Test
    void toggleActiveNaoDevePermitirDesativarProprioUsuario() {
        assertThatThrownBy(() -> service.toggleActive(adminLogado.getId(), "127.0.0.1"))
                .isInstanceOf(ConflictException.class)
                .hasMessageContaining("próprio");
    }
}

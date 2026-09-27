package br.com.consultorio.audit;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.repository.AuditLogRepository;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuditServiceTest {

    @Mock private AuditLogRepository repository;

    @InjectMocks private AuditService service;

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private AppUser usuarioAutenticado(String nome) {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "name", nome);
        ReflectionTestUtils.setField(user, "email", "user@consultorio.com");
        ReflectionTestUtils.setField(user, "role", AppUser.Role.ADMIN);
        ReflectionTestUtils.setField(user, "active", true);
        return user;
    }

    private void autenticar(AppUser user) {
        SecurityContextHolder.getContext().setAuthentication(
                new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities())
        );
    }

    @Test
    void logDevePersistitRegistroComDadosDoUsuarioAutenticado() {
        AppUser user = usuarioAutenticado("Dr. Valdomiro");
        autenticar(user);
        UUID entityId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.log(AuditLog.Action.CREATE, "PATIENT", entityId, "Paciente criado", "127.0.0.1");

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());

        AuditLog salvo = captor.getValue();
        assertThat(salvo.getUserId()).isEqualTo(user.getId());
        assertThat(salvo.getUserName()).isEqualTo("Dr. Valdomiro");
        assertThat(salvo.getAction()).isEqualTo("CREATE");
        assertThat(salvo.getEntity()).isEqualTo("PATIENT");
        assertThat(salvo.getEntityId()).isEqualTo(entityId);
        assertThat(salvo.getDetails()).isEqualTo("Paciente criado");
        assertThat(salvo.getIp()).isEqualTo("127.0.0.1");
        assertThat(salvo.getCreatedAt()).isNotNull();
    }

    @Test
    void logDevePersistirComUserSystemQuandoSemAutenticacao() {
        UUID entityId = UUID.randomUUID();
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        service.log(AuditLog.Action.DELETE, "PATIENT", entityId, "Remoção automática", null);

        ArgumentCaptor<AuditLog> captor = ArgumentCaptor.forClass(AuditLog.class);
        verify(repository).save(captor.capture());

        AuditLog salvo = captor.getValue();
        assertThat(salvo.getUserId()).isNull();
        assertThat(salvo.getUserName()).isEqualTo("system");
        assertThat(salvo.getAction()).isEqualTo("DELETE");
        assertThat(salvo.getIp()).isNull();
    }

    @Test
    void logDeveRegistrarTodosOsTiposDeAcao() {
        AppUser user = usuarioAutenticado("Secretária");
        autenticar(user);
        when(repository.save(any())).thenAnswer(i -> i.getArgument(0));

        for (AuditLog.Action action : AuditLog.Action.values()) {
            service.log(action, "PATIENT", UUID.randomUUID(), "teste", "10.0.0.1");
        }

        verify(repository, org.mockito.Mockito.times(AuditLog.Action.values().length)).save(any());
    }
}

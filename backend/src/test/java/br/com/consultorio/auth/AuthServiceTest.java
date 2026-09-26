package br.com.consultorio.auth;

import br.com.consultorio.auth.dto.LoginRequest;
import br.com.consultorio.auth.dto.LoginResponse;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.entity.RefreshToken;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.auth.repository.RefreshTokenRepository;
import br.com.consultorio.auth.service.AuthService;
import br.com.consultorio.auth.service.JwtService;
import br.com.consultorio.shared.exception.UnauthorizedException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock private AppUserRepository userRepository;
    @Mock private RefreshTokenRepository refreshTokenRepository;
    @Mock private JwtService jwtService;
    @Mock private PasswordEncoder passwordEncoder;

    @InjectMocks
    private AuthService authService;

    private AppUser usuarioAtivo() {
        AppUser user = new AppUser();
        ReflectionTestUtils.setField(user, "id", UUID.randomUUID());
        ReflectionTestUtils.setField(user, "email", "admin@consultorio.com");
        ReflectionTestUtils.setField(user, "passwordHash", "$2a$12$hash");
        ReflectionTestUtils.setField(user, "name", "Administrador");
        ReflectionTestUtils.setField(user, "role", AppUser.Role.ADMIN);
        ReflectionTestUtils.setField(user, "active", true);
        return user;
    }

    @Test
    void loginDeveRetornarTokensQuandoCredenciaisValidas() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpirationDays", 30L);
        AppUser user = usuarioAtivo();

        when(userRepository.findByEmail("admin@consultorio.com")).thenReturn(Optional.of(user));
        when(passwordEncoder.matches("Admin@123", "$2a$12$hash")).thenReturn(true);
        when(jwtService.generateAccessToken("admin@consultorio.com", "ADMIN")).thenReturn("access.token.jwt");
        when(refreshTokenRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        LoginResponse response = authService.login(new LoginRequest("admin@consultorio.com", "Admin@123"));

        assertThat(response.accessToken()).isEqualTo("access.token.jwt");
        assertThat(response.refreshToken()).isNotBlank();
        assertThat(response.name()).isEqualTo("Administrador");
        assertThat(response.role()).isEqualTo("ADMIN");
        verify(refreshTokenRepository).save(any(RefreshToken.class));
    }

    @Test
    void loginDeveLancarExcecaoQuandoUsuarioNaoEncontrado() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login(new LoginRequest("nao@existe.com", "senha")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    void loginDeveLancarExcecaoQuandoSenhaIncorreta() {
        AppUser user = usuarioAtivo();
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));
        when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@consultorio.com", "senhaErrada")))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Credenciais inválidas");
    }

    @Test
    void loginDeveLancarExcecaoQuandoUsuarioInativo() {
        AppUser user = usuarioAtivo();
        ReflectionTestUtils.setField(user, "active", false);
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(user));

        assertThatThrownBy(() -> authService.login(new LoginRequest("admin@consultorio.com", "Admin@123")))
                .isInstanceOf(UnauthorizedException.class);
    }

    @Test
    void refreshDeveRetornarNovoAccessToken() {
        AppUser user = usuarioAtivo();
        RefreshToken refreshToken = RefreshToken.builder()
                .user(user)
                .token("refresh-token-valido")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(refreshTokenRepository.findByToken("refresh-token-valido")).thenReturn(Optional.of(refreshToken));
        when(jwtService.generateAccessToken("admin@consultorio.com", "ADMIN")).thenReturn("novo.access.token");

        LoginResponse response = authService.refresh("refresh-token-valido");

        assertThat(response.accessToken()).isEqualTo("novo.access.token");
        assertThat(response.refreshToken()).isEqualTo("refresh-token-valido");
    }

    @Test
    void refreshDeveLancarExcecaoQuandoTokenNaoEncontrado() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("token-inexistente"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token inválido");
    }

    @Test
    void refreshDeveLancarExcecaoEDeletarTokenExpirado() {
        AppUser user = usuarioAtivo();
        RefreshToken tokenExpirado = RefreshToken.builder()
                .user(user)
                .token("token-expirado")
                .expiresAt(LocalDateTime.now().minusDays(1))
                .build();

        when(refreshTokenRepository.findByToken("token-expirado")).thenReturn(Optional.of(tokenExpirado));

        assertThatThrownBy(() -> authService.refresh("token-expirado"))
                .isInstanceOf(UnauthorizedException.class)
                .hasMessage("Refresh token expirado, faça login novamente");

        verify(refreshTokenRepository).delete(tokenExpirado);
    }

    @Test
    void logoutDeveDeletarRefreshToken() {
        AppUser user = usuarioAtivo();
        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token("token-para-logout")
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        when(refreshTokenRepository.findByToken("token-para-logout")).thenReturn(Optional.of(token));

        authService.logout("token-para-logout");

        verify(refreshTokenRepository).delete(token);
    }

    @Test
    void logoutNaoDeveLancarExcecaoQuandoTokenNaoExiste() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        authService.logout("token-inexistente");

        verify(refreshTokenRepository, never()).delete(any());
    }
}

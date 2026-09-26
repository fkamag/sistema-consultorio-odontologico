package br.com.consultorio.auth;

import br.com.consultorio.auth.service.JwtService;
import io.jsonwebtoken.Claims;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtServiceTest {

    private JwtService jwtService;

    private static final String SECRET = "test_secret_key_com_no_minimo_256_bits_para_os_testes_unitarios_aqui";
    private static final long EXPIRATION_MINUTES = 60;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService(SECRET, EXPIRATION_MINUTES);
    }

    @Test
    void deveGerarTokenValido() {
        String token = jwtService.generateAccessToken("user@test.com", "ADMIN");

        assertThat(token).isNotBlank();
        assertThat(jwtService.isTokenValid(token)).isTrue();
    }

    @Test
    void deveExtrairEmailDoToken() {
        String email = "user@test.com";
        String token = jwtService.generateAccessToken(email, "SECRETARIA");

        assertThat(jwtService.extractEmail(token)).isEqualTo(email);
    }

    @Test
    void deveExtrairRoleDoToken() {
        String token = jwtService.generateAccessToken("user@test.com", "DENTISTA");

        Claims claims = jwtService.parseToken(token);
        assertThat(claims.get("role", String.class)).isEqualTo("DENTISTA");
    }

    @Test
    void deveRetornarFalseParaTokenInvalido() {
        assertThat(jwtService.isTokenValid("token.invalido.aqui")).isFalse();
    }

    @Test
    void deveRetornarFalseParaTokenExpirado() {
        JwtService serviceComExpiracaoImediata = new JwtService(SECRET, 0);
        String token = serviceComExpiracaoImediata.generateAccessToken("user@test.com", "ADMIN");

        assertThat(jwtService.isTokenValid(token)).isFalse();
    }
}

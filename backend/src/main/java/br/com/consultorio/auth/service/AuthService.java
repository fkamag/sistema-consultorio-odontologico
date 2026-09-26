package br.com.consultorio.auth.service;

import br.com.consultorio.auth.dto.LoginRequest;
import br.com.consultorio.auth.dto.LoginResponse;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.entity.RefreshToken;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.auth.repository.RefreshTokenRepository;
import br.com.consultorio.shared.exception.UnauthorizedException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final AppUserRepository userRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final JwtService jwtService;
    private final PasswordEncoder passwordEncoder;

    @Value("${app.jwt.refresh-token-expiration-days}")
    private long refreshTokenExpirationDays;

    @Transactional
    public LoginResponse login(LoginRequest request) {
        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new UnauthorizedException("Credenciais inválidas"));

        if (!user.isEnabled() || !passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new UnauthorizedException("Credenciais inválidas");
        }

        String accessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());
        String refreshToken = createRefreshToken(user);

        return new LoginResponse(accessToken, refreshToken, user.getName(), user.getRole().name());
    }

    @Transactional
    public LoginResponse refresh(String rawRefreshToken) {
        RefreshToken token = refreshTokenRepository.findByToken(rawRefreshToken)
                .orElseThrow(() -> new UnauthorizedException("Refresh token inválido"));

        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new UnauthorizedException("Refresh token expirado, faça login novamente");
        }

        AppUser user = token.getUser();
        String newAccessToken = jwtService.generateAccessToken(user.getEmail(), user.getRole().name());

        return new LoginResponse(newAccessToken, rawRefreshToken, user.getName(), user.getRole().name());
    }

    @Transactional
    public void logout(String rawRefreshToken) {
        refreshTokenRepository.findByToken(rawRefreshToken)
                .ifPresent(refreshTokenRepository::delete);
    }

    private String createRefreshToken(AppUser user) {
        byte[] bytes = new byte[64];
        new SecureRandom().nextBytes(bytes);
        String tokenValue = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        RefreshToken token = RefreshToken.builder()
                .user(user)
                .token(tokenValue)
                .expiresAt(LocalDateTime.now().plusDays(refreshTokenExpirationDays))
                .build();

        refreshTokenRepository.save(token);
        return tokenValue;
    }
}

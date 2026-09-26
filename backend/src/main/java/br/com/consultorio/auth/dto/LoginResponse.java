package br.com.consultorio.auth.dto;

public record LoginResponse(
        String accessToken,
        String refreshToken,
        String name,
        String role
) {}

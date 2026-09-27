package br.com.consultorio.user.dto;

import br.com.consultorio.auth.entity.AppUser;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email,
        String role,
        boolean active,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static UserResponse from(AppUser u) {
        return new UserResponse(
                u.getId(),
                u.getName(),
                u.getEmail(),
                u.getRole().name(),
                u.isActive(),
                u.getCreatedAt(),
                u.getUpdatedAt()
        );
    }
}

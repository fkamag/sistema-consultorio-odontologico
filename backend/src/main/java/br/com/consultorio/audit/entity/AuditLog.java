package br.com.consultorio.audit.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "audit_log")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AuditLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private UUID userId;

    @Column(nullable = false, length = 150)
    private String userName;

    @Column(nullable = false, length = 20)
    private String action;

    @Column(nullable = false, length = 50)
    private String entity;

    private UUID entityId;

    @Column(columnDefinition = "TEXT")
    private String details;

    @Column(length = 45)
    private String ip;

    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Action {
        CREATE, UPDATE, DELETE, LOGIN, LOGOUT
    }
}

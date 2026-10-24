package br.com.consultorio.consent.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "consents")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Consent {

    public enum ConsentType {
        TRATAMENTO_GERAL, PROCEDIMENTO_CIRURGICO, ANESTESIA, FOTOGRAFIAS, MENOR
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false, length = 200)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private ConsentType type;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    private LocalDateTime signedAt;

    @Column(length = 150)
    private String signedByName;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private UUID createdById;

    @Column(length = 150)
    private String createdByName;
}

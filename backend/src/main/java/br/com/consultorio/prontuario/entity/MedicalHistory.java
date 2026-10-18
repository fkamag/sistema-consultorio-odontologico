package br.com.consultorio.prontuario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "medical_histories")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class MedicalHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    @Builder.Default
    private int versionNumber = 1;

    @Column(columnDefinition = "TEXT")
    private String alergias;

    @Column(columnDefinition = "TEXT")
    private String medicamentos;

    @Column(nullable = false)
    @Builder.Default
    private boolean diabetes = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean hipertensao = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean problemasCardiacos = false;

    @Column(columnDefinition = "TEXT")
    private String tratamentosMedicos;

    @Column(columnDefinition = "TEXT")
    private String cirurgias;

    @Column(columnDefinition = "TEXT")
    private String observacoes;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private UUID createdById;
    private String createdByName;
}

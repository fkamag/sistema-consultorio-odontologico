package br.com.consultorio.prontuario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "treatment_plan_items")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class TreatmentPlanItem {

    public enum Status {
        PLANEJADO, APROVADO, PARCIALMENTE_APROVADO, REALIZADO, CANCELADO, PENDENTE
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false, length = 300)
    private String description;

    @Column(length = 10)
    private String tooth;

    @Column(precision = 10, scale = 2)
    private BigDecimal valueEstimated;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private Status status = Status.PLANEJADO;

    private UUID appointmentId;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private UUID createdById;
    private String createdByName;
    private UUID updatedById;
    private String updatedByName;
}

package br.com.consultorio.budget.entity;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "budgets")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Budget {

    public enum BudgetStatus {
        RASCUNHO, ENVIADO, APROVADO, REPROVADO, EXPIRADO
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    private UUID dentistId;

    @Column(length = 150)
    private String dentistName;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private BudgetStatus status = BudgetStatus.RASCUNHO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    private LocalDate validUntil;

    @Column(nullable = false, precision = 12, scale = 2)
    @Builder.Default
    private BigDecimal totalValue = BigDecimal.ZERO;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime updatedAt = LocalDateTime.now();

    private UUID createdById;

    @Column(length = 150)
    private String createdByName;

    private UUID updatedById;

    @Column(length = 150)
    private String updatedByName;

    @PreUpdate
    void onUpdate() {
        this.updatedAt = LocalDateTime.now();
    }
}

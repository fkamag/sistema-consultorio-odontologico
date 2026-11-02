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
        EM_ELABORACAO, ENVIADO, AGUARDANDO_APROVACAO, APROVADO,
        PARCIALMENTE_APROVADO, RECUSADO, CANCELADO, CONCLUIDO
    }

    public enum TratamentoStatus {
        NAO_INICIADO, EM_ANDAMENTO, FINALIZADO
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
    private BudgetStatus status = BudgetStatus.EM_ELABORACAO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(name = "descricao_itens", columnDefinition = "TEXT")
    private String descricaoItens;

    @Enumerated(EnumType.STRING)
    @Column(name = "status_tratamento", nullable = false, length = 30)
    @Builder.Default
    private TratamentoStatus statusTratamento = TratamentoStatus.NAO_INICIADO;

    @Column(name = "situacao_saldo", length = 100)
    private String situacaoSaldo;

    @Column(name = "declaracao_ir", length = 50)
    private String declaracaoIr;

    @Column(name = "data_declaracao")
    private LocalDate dataDeclaracao;

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

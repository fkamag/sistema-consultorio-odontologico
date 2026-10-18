package br.com.consultorio.prontuario.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clinical_notes")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClinicalNote {

    public enum NoteType {
        CONSULTA, PROCEDIMENTO, RETORNO, OBSERVACAO, INTERCORRENCIA
    }

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    private UUID dentistId;

    private UUID appointmentId;

    @Column(nullable = false)
    private LocalDate noteDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    @Builder.Default
    private NoteType noteType = NoteType.CONSULTA;

    @Column(length = 10)
    private String tooth;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private UUID createdById;
    private String createdByName;
}

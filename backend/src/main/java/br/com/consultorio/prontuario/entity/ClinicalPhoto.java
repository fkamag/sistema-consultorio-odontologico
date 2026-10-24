package br.com.consultorio.prontuario.entity;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clinical_photos")
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class ClinicalPhoto {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    private UUID clinicalNoteId;

    @Column(nullable = false)
    private String objectName;

    @Column(length = 200)
    private String caption;

    @Column(nullable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    private UUID createdById;

    @Column(length = 150)
    private String createdByName;
}

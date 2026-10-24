package br.com.consultorio.odontogram.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "tooth_records")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ToothRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false, length = 3)
    private String toothNumber;

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String vestibular = "";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String mesial = "";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String distal = "";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String lingual = "";

    @Column(nullable = false, length = 20)
    @Builder.Default
    private String oclusal = "";

    @Column(nullable = false)
    @Builder.Default
    private boolean canal = false;

    @Column(nullable = false)
    @Builder.Default
    private boolean ausente = false;

    @Column(columnDefinition = "TEXT")
    private String note;

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

package br.com.consultorio.appointment.entity;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID patientId;

    @Column(nullable = false)
    private UUID dentistId;

    @Column(nullable = false)
    private LocalDateTime startTime;

    @Column(nullable = false)
    @Builder.Default
    private int durationMinutes = 30;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private Status status = Status.AGENDADO;

    @Column(columnDefinition = "TEXT")
    private String notes;

    @Column(nullable = false, updatable = false)
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

    public LocalDateTime endTime() {
        return startTime.plusMinutes(durationMinutes);
    }

    public enum Status {
        AGENDADO, CONFIRMADO, REALIZADO, CANCELADO
    }
}

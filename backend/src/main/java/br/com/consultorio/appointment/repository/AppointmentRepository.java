package br.com.consultorio.appointment.repository;

import br.com.consultorio.appointment.entity.Appointment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface AppointmentRepository extends JpaRepository<Appointment, UUID> {

    List<Appointment> findByDentistIdAndStartTimeBetweenOrderByStartTime(
            UUID dentistId, LocalDateTime from, LocalDateTime to);

    List<Appointment> findByPatientIdOrderByStartTimeDesc(UUID patientId);

    @Query("""
            SELECT COUNT(a) > 0 FROM Appointment a
            WHERE a.dentistId = :dentistId
              AND a.status NOT IN ('CANCELADO')
              AND a.startTime < :end
              AND a.startTime >= :start
              AND (:excludeId IS NULL OR a.id <> :excludeId)
            """)
    boolean existsConflict(
            @Param("dentistId") UUID dentistId,
            @Param("start") LocalDateTime start,
            @Param("end") LocalDateTime end,
            @Param("excludeId") UUID excludeId
    );
}

package br.com.consultorio.appointment.controller;

import br.com.consultorio.appointment.dto.AppointmentRequest;
import br.com.consultorio.appointment.dto.AppointmentResponse;
import br.com.consultorio.appointment.entity.Appointment;
import br.com.consultorio.appointment.service.AppointmentService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/agendamentos")
@RequiredArgsConstructor
public class AppointmentController {

    private final AppointmentService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<AppointmentResponse> listByPeriod(
            @RequestParam(required = false) UUID dentistId,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to) {
        return service.findByPeriod(
                dentistId,
                from.atStartOfDay(),
                to.atTime(23, 59, 59));
    }

    @GetMapping("/paciente/{patientId}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<AppointmentResponse> listByPatient(@PathVariable UUID patientId) {
        return service.findByPatient(patientId);
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public AppointmentResponse create(
            @Valid @RequestBody AppointmentRequest request,
            HttpServletRequest httpRequest) {
        return service.create(request, resolveIp(httpRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public AppointmentResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody AppointmentRequest request,
            HttpServletRequest httpRequest) {
        return service.update(id, request, resolveIp(httpRequest));
    }

    @PatchMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public AppointmentResponse changeStatus(
            @PathVariable UUID id,
            @RequestParam Appointment.Status status,
            HttpServletRequest httpRequest) {
        return service.changeStatus(id, status, resolveIp(httpRequest));
    }

    private String resolveIp(HttpServletRequest request) {
        String forwarded = request.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : request.getRemoteAddr();
    }
}

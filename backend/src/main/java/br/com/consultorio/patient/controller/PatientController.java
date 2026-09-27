package br.com.consultorio.patient.controller;

import br.com.consultorio.patient.dto.PatientRequest;
import br.com.consultorio.patient.dto.PatientResponse;
import br.com.consultorio.patient.service.PatientService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.UUID;

@RestController
@RequestMapping("/api/pacientes")
@RequiredArgsConstructor
public class PatientController {

    private final PatientService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public Page<PatientResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return service.list(search, page, size);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public PatientResponse findById(@PathVariable UUID id) {
        return service.findById(id);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public ResponseEntity<PatientResponse> create(
            @Valid @RequestBody PatientRequest request,
            HttpServletRequest httpRequest) {
        PatientResponse created = service.create(request, resolveIp(httpRequest));
        return ResponseEntity
                .created(URI.create("/api/pacientes/" + created.id()))
                .body(created);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public PatientResponse update(
            @PathVariable UUID id,
            @Valid @RequestBody PatientRequest request,
            HttpServletRequest httpRequest) {
        return service.update(id, request, resolveIp(httpRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA')")
    public ResponseEntity<Void> delete(@PathVariable UUID id, HttpServletRequest httpRequest) {
        service.delete(id, resolveIp(httpRequest));
        return ResponseEntity.noContent().build();
    }

    private String resolveIp(HttpServletRequest req) {
        String forwarded = req.getHeader("X-Forwarded-For");
        return (forwarded != null && !forwarded.isBlank())
                ? forwarded.split(",")[0].trim()
                : req.getRemoteAddr();
    }
}

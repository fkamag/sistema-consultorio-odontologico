package br.com.consultorio.consent.controller;

import br.com.consultorio.consent.dto.ConsentRequest;
import br.com.consultorio.consent.dto.ConsentResponse;
import br.com.consultorio.consent.service.ConsentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients/{patientId}/consents")
@RequiredArgsConstructor
public class ConsentController {

    private final ConsentService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<ConsentResponse> list(@PathVariable UUID patientId) {
        return service.listByPatient(patientId);
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public ResponseEntity<ConsentResponse> create(
            @PathVariable UUID patientId,
            @Valid @RequestBody ConsentRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        ConsentResponse created = service.create(patientId, request, ip);
        return ResponseEntity
                .created(URI.create("/api/patients/" + patientId + "/consents/" + created.id()))
                .body(created);
    }

    @PostMapping("/{consentId}/sign")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public ConsentResponse sign(
            @PathVariable UUID patientId,
            @PathVariable UUID consentId,
            @RequestParam String signerName,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        return service.sign(patientId, consentId, signerName, ip);
    }

    @DeleteMapping("/{consentId}")
    @PreAuthorize("hasAnyRole('ADMIN')")
    public ResponseEntity<Void> delete(
            @PathVariable UUID patientId,
            @PathVariable UUID consentId,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        service.delete(patientId, consentId, ip);
        return ResponseEntity.noContent().build();
    }
}

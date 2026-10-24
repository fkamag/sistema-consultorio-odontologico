package br.com.consultorio.odontogram.controller;

import br.com.consultorio.odontogram.dto.ToothRecordRequest;
import br.com.consultorio.odontogram.dto.ToothRecordResponse;
import br.com.consultorio.odontogram.service.OdontogramService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/patients/{patientId}/odontogram")
@RequiredArgsConstructor
public class OdontogramController {

    private final OdontogramService service;

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<ToothRecordResponse> list(@PathVariable UUID patientId) {
        return service.listByPatient(patientId);
    }

    @PutMapping("/{toothNumber}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA')")
    public ToothRecordResponse upsert(
            @PathVariable UUID patientId,
            @PathVariable String toothNumber,
            @Valid @RequestBody ToothRecordRequest request,
            @RequestHeader(value = "X-Forwarded-For", required = false) String ip) {
        return service.upsert(patientId, request, ip);
    }
}

package br.com.consultorio.audit.controller;

import br.com.consultorio.audit.dto.AuditResponse;
import br.com.consultorio.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return repository.search(search, PageRequest.of(page, size))
                .map(AuditResponse::from);
    }
}

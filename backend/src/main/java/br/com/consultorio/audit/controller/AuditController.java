package br.com.consultorio.audit.controller;

import br.com.consultorio.audit.dto.AuditResponse;
import br.com.consultorio.audit.repository.AuditLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/auditoria")
@RequiredArgsConstructor
public class AuditController {

    private final AuditLogRepository repository;

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public Page<AuditResponse> list(
            @RequestParam(defaultValue = "") String search,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate from,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate to,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {

        LocalDateTime fromDt = from != null ? from.atStartOfDay()   : LocalDateTime.of(2000, 1, 1, 0, 0);
        LocalDateTime toDt   = to   != null ? to.atTime(23, 59, 59) : LocalDateTime.of(2099, 12, 31, 23, 59, 59);

        return repository.search(search, fromDt, toDt, PageRequest.of(page, size))
                .map(AuditResponse::from);
    }
}

package br.com.consultorio.consent.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.consent.dto.ConsentRequest;
import br.com.consultorio.consent.dto.ConsentResponse;
import br.com.consultorio.consent.entity.Consent;
import br.com.consultorio.consent.repository.ConsentRepository;
import br.com.consultorio.shared.exception.NotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ConsentService {

    private final ConsentRepository repository;
    private final AuditService auditService;

    public List<ConsentResponse> listByPatient(UUID patientId) {
        return repository.findByPatientIdOrderByCreatedAtDesc(patientId).stream()
                .map(ConsentResponse::from)
                .toList();
    }

    @Transactional
    public ConsentResponse create(UUID patientId, ConsentRequest req, String ip) {
        AppUser actor = currentUser();
        Consent consent = Consent.builder()
                .patientId(patientId)
                .title(req.title())
                .type(req.type())
                .content(req.content())
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .build();
        Consent saved = repository.save(consent);
        auditService.log(AuditLog.Action.CREATE, "CONSENT", saved.getId(),
                "Consentimento criado: " + saved.getTitle(), ip);
        return ConsentResponse.from(saved);
    }

    @Transactional
    public ConsentResponse sign(UUID patientId, UUID consentId, String signerName, String ip) {
        Consent consent = repository.findByIdAndPatientId(consentId, patientId)
                .orElseThrow(() -> new NotFoundException("Consentimento não encontrado."));
        consent.setSignedAt(LocalDateTime.now());
        consent.setSignedByName(signerName);
        Consent saved = repository.save(consent);
        auditService.log(AuditLog.Action.UPDATE, "CONSENT", saved.getId(),
                "Consentimento assinado por: " + signerName, ip);
        return ConsentResponse.from(saved);
    }

    @Transactional
    public void delete(UUID patientId, UUID consentId, String ip) {
        Consent consent = repository.findByIdAndPatientId(consentId, patientId)
                .orElseThrow(() -> new NotFoundException("Consentimento não encontrado."));
        repository.delete(consent);
        auditService.log(AuditLog.Action.DELETE, "CONSENT", consentId,
                "Consentimento removido do paciente " + patientId, ip);
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

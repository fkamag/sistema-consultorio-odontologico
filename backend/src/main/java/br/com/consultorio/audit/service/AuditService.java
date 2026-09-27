package br.com.consultorio.audit.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.repository.AuditLogRepository;
import br.com.consultorio.auth.entity.AppUser;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AuditService {

    private final AuditLogRepository repository;

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void log(AuditLog.Action action, String entity, UUID entityId, String details, String ip) {
        AppUser currentUser = currentUser();
        AuditLog log = AuditLog.builder()
                .userId(currentUser != null ? currentUser.getId() : null)
                .userName(currentUser != null ? currentUser.getName() : "system")
                .action(action.name())
                .entity(entity)
                .entityId(entityId)
                .details(details)
                .ip(ip)
                .build();
        repository.save(log);
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

package br.com.consultorio.user.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.auth.repository.AppUserRepository;
import br.com.consultorio.shared.exception.ConflictException;
import br.com.consultorio.shared.exception.NotFoundException;
import br.com.consultorio.user.dto.UserRequest;
import br.com.consultorio.user.dto.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class UserService {

    private final AppUserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final AuditService auditService;

    public Page<UserResponse> list(String search, int page, int size) {
        return repository.search(search, PageRequest.of(page, size))
                .map(UserResponse::from);
    }

    public UserResponse findById(UUID id) {
        return UserResponse.from(getOrThrow(id));
    }

    @Transactional
    public UserResponse create(UserRequest request, String ip) {
        if (repository.existsByEmail(request.email())) {
            throw new ConflictException("Já existe um usuário cadastrado com este e-mail.");
        }
        AppUser user = AppUser.builder()
                .name(request.name())
                .email(request.email())
                .passwordHash(passwordEncoder.encode(request.password()))
                .role(AppUser.Role.valueOf(request.role()))
                .build();
        AppUser saved = repository.save(user);
        auditService.log(AuditLog.Action.CREATE, "USER", saved.getId(),
                "Usuário criado: " + saved.getName() + " (" + saved.getRole() + ")", ip);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse update(UUID id, UserRequest request, String ip) {
        AppUser user = getOrThrow(id);
        if (repository.existsByEmailAndIdNot(request.email(), id)) {
            throw new ConflictException("Já existe outro usuário cadastrado com este e-mail.");
        }
        AppUser actor = currentUser();
        AppUser.Role novoRole = AppUser.Role.valueOf(request.role());
        if (actor != null && actor.getId().equals(id) && novoRole != user.getRole()) {
            throw new ConflictException("Não é possível alterar o próprio perfil.");
        }
        user.setName(request.name());
        user.setEmail(request.email());
        user.setRole(novoRole);
        if (request.password() != null && !request.password().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.password()));
        }
        AppUser saved = repository.save(user);
        auditService.log(AuditLog.Action.UPDATE, "USER", saved.getId(),
                "Usuário atualizado: " + saved.getName(), ip);
        return UserResponse.from(saved);
    }

    @Transactional
    public UserResponse toggleActive(UUID id, String ip) {
        AppUser actor = currentUser();
        if (actor != null && actor.getId().equals(id)) {
            throw new ConflictException("Não é possível desativar o próprio usuário.");
        }
        AppUser user = getOrThrow(id);
        user.setActive(!user.isActive());
        AppUser saved = repository.save(user);
        String acao = saved.isActive() ? "ativado" : "desativado";
        auditService.log(AuditLog.Action.UPDATE, "USER", saved.getId(),
                "Usuário " + acao + ": " + saved.getName(), ip);
        return UserResponse.from(saved);
    }

    private AppUser getOrThrow(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new NotFoundException("Usuário não encontrado."));
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

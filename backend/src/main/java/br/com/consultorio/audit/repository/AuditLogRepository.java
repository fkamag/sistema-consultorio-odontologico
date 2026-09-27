package br.com.consultorio.audit.repository;

import br.com.consultorio.audit.entity.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface AuditLogRepository extends JpaRepository<AuditLog, UUID> {

    @Query("""
            SELECT a FROM AuditLog a
            WHERE (:search IS NULL OR :search = ''
                   OR LOWER(a.userName) LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(a.entity)   LIKE LOWER(CONCAT('%', :search, '%'))
                   OR LOWER(a.action)   LIKE LOWER(CONCAT('%', :search, '%')))
            ORDER BY a.createdAt DESC
            """)
    Page<AuditLog> search(@Param("search") String search, Pageable pageable);

    Page<AuditLog> findByEntityAndEntityIdOrderByCreatedAtDesc(String entity, UUID entityId, Pageable pageable);
}

package br.com.consultorio.odontogram.service;

import br.com.consultorio.audit.entity.AuditLog;
import br.com.consultorio.audit.service.AuditService;
import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.odontogram.dto.ToothRecordRequest;
import br.com.consultorio.odontogram.dto.ToothRecordResponse;
import br.com.consultorio.odontogram.entity.ToothRecord;
import br.com.consultorio.odontogram.repository.ToothRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class OdontogramService {

    private final ToothRecordRepository repository;
    private final AuditService auditService;

    public List<ToothRecordResponse> listByPatient(UUID patientId) {
        return repository.findByPatientId(patientId).stream()
                .map(ToothRecordResponse::from)
                .toList();
    }

    @Transactional
    public ToothRecordResponse upsert(UUID patientId, ToothRecordRequest req, String ip) {
        AppUser actor = currentUser();
        ToothRecord record = repository.findByPatientIdAndToothNumber(patientId, req.toothNumber())
                .orElse(null);
        AuditLog.Action action;
        if (record == null) {
            record = ToothRecord.builder()
                    .patientId(patientId)
                    .toothNumber(req.toothNumber())
                    .vestibular(nvl(req.vestibular()))
                    .mesial(nvl(req.mesial()))
                    .distal(nvl(req.distal()))
                    .lingual(nvl(req.lingual()))
                    .oclusal(nvl(req.oclusal()))
                    .canal(req.canal())
                    .ausente(req.ausente())
                    .note(req.note())
                    .createdById(actor != null ? actor.getId() : null)
                    .createdByName(actor != null ? actor.getName() : null)
                    .updatedById(actor != null ? actor.getId() : null)
                    .updatedByName(actor != null ? actor.getName() : null)
                    .build();
            action = AuditLog.Action.CREATE;
        } else {
            record.setVestibular(nvl(req.vestibular()));
            record.setMesial(nvl(req.mesial()));
            record.setDistal(nvl(req.distal()));
            record.setLingual(nvl(req.lingual()));
            record.setOclusal(nvl(req.oclusal()));
            record.setCanal(req.canal());
            record.setAusente(req.ausente());
            record.setNote(req.note());
            record.setUpdatedById(actor != null ? actor.getId() : null);
            record.setUpdatedByName(actor != null ? actor.getName() : null);
            action = AuditLog.Action.UPDATE;
        }
        ToothRecord saved = repository.save(record);
        auditService.log(action, "TOOTH_RECORD", saved.getId(),
                "Odontograma dente " + req.toothNumber(), ip);
        return ToothRecordResponse.from(saved);
    }

    private String nvl(String s) { return s == null ? "" : s; }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) {
            return user;
        }
        return null;
    }
}

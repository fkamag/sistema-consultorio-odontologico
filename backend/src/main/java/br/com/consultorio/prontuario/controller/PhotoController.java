package br.com.consultorio.prontuario.controller;

import br.com.consultorio.auth.entity.AppUser;
import br.com.consultorio.prontuario.dto.ClinicalPhotoResponse;
import br.com.consultorio.prontuario.entity.ClinicalPhoto;
import br.com.consultorio.prontuario.repository.ClinicalPhotoRepository;
import br.com.consultorio.storage.StorageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.InputStream;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PhotoController {

    private final ClinicalPhotoRepository photoRepository;
    private final StorageService storageService;

    @GetMapping("/api/patients/{patientId}/photos")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public List<ClinicalPhotoResponse> listByPatient(@PathVariable UUID patientId) {
        return photoRepository.findByPatientIdOrderByCreatedAtDesc(patientId)
                .stream().map(ClinicalPhotoResponse::from).toList();
    }

    @PostMapping("/api/patients/{patientId}/photos")
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA')")
    public ClinicalPhotoResponse upload(
            @PathVariable UUID patientId,
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam(value = "clinicalNoteId", required = false) UUID clinicalNoteId) {
        AppUser actor = currentUser();
        String objectName = storageService.upload(file, "patients/" + patientId);
        ClinicalPhoto photo = ClinicalPhoto.builder()
                .patientId(patientId)
                .clinicalNoteId(clinicalNoteId)
                .objectName(objectName)
                .caption(caption)
                .createdById(actor != null ? actor.getId() : null)
                .createdByName(actor != null ? actor.getName() : null)
                .build();
        return ClinicalPhotoResponse.from(photoRepository.save(photo));
    }

    @GetMapping("/api/photos/{id}/content")
    @PreAuthorize("hasAnyRole('ADMIN','SECRETARIA','DENTISTA')")
    public ResponseEntity<byte[]> download(@PathVariable UUID id) {
        ClinicalPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada"));
        try (InputStream is = storageService.download(photo.getObjectName())) {
            byte[] bytes = is.readAllBytes();
            String contentType = photo.getObjectName().endsWith(".png") ? "image/png" : "image/jpeg";
            return ResponseEntity.ok().contentType(MediaType.parseMediaType(contentType)).body(bytes);
        } catch (Exception e) {
            throw new RuntimeException("Erro ao baixar foto", e);
        }
    }

    @DeleteMapping("/api/patients/{patientId}/photos/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DENTISTA')")
    public ResponseEntity<Void> delete(@PathVariable UUID patientId, @PathVariable UUID id) {
        ClinicalPhoto photo = photoRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Foto não encontrada"));
        storageService.delete(photo.getObjectName());
        photoRepository.delete(photo);
        return ResponseEntity.noContent().build();
    }

    private AppUser currentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null && auth.getPrincipal() instanceof AppUser user) return user;
        return null;
    }
}

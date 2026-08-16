package com.hospital.hms.controller;
 
import com.hospital.hms.dto.response.AuditLogResponseDTO;
import com.hospital.hms.model.AuditLog;
import com.hospital.hms.repository.AuditLogRepository;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Locale;
 
@Tag(name = "Audit Log", description = "Admin-only activity history")
@RestController
@RequestMapping("/api/audit-logs")
public class AuditLogController {
 
    @Autowired
    private AuditLogRepository auditLogRepository;
 
    private AuditLogResponseDTO toDTO(AuditLog a) {
        return new AuditLogResponseDTO(
            a.getId(),
            a.getUser() != null ? a.getUser().getName() : "System",
            a.getUser() != null ? a.getUser().getRole().toString() : null,
            a.getAction(),
            a.getEntityType(),
            a.getEntityId(),
            a.getDetails(),
            a.getCreatedAt() != null ? a.getCreatedAt().toString() : null
        );
    }
 
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AuditLogResponseDTO>> getAuditLogs(
            @RequestParam(required = false) String action,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "30") int size) {
 
        Specification<AuditLog> spec = Specification
                .<AuditLog>where((root, query, cb) -> cb.conjunction());
        if (action != null && !action.isBlank()) {
            String normalizedAction = action.trim()
                    .replaceAll("[^A-Za-z0-9]+", "_")
                    .replaceAll("_+", "_")
                    .replaceAll("^_|_$", "")
                    .toUpperCase(Locale.ROOT);

            final String dbAction;
            if ("DOCTOR_ONBOARDING".equals(normalizedAction)) {
                dbAction = "DOCTOR_ONBOARDED";
            } else if ("DOCTOR_DELETING".equals(normalizedAction)) {
                dbAction = "DOCTOR_DELETED";
            } else if ("PATIENT_REGISTERING".equals(normalizedAction)) {
                dbAction = "PATIENT_REGISTERED";
            } else if ("RECORD_ADDING".equals(normalizedAction)) {
                dbAction = "RECORD_CREATED";
            } else if ("BILL_GENERATING".equals(normalizedAction)) {
                dbAction = "BILL_CREATED";
            } else if ("PAYMENT_RECEIVED".equals(normalizedAction)) {
                dbAction = "BILL_PAID";
            } else if ("APPOINTMENT_BOOKING".equals(normalizedAction)) {
                dbAction = "APPOINTMENT_BOOKED";
            } else if ("APPOINTMENT_STATUS_UPDATE".equals(normalizedAction)) {
                dbAction = "APPOINTMENT_STATUS_CHANGED";
            } else if ("APPOINTMENT_CANCELLATION".equals(normalizedAction)) {
                dbAction = "APPOINTMENT_CANCELLED";
            } else {
                dbAction = normalizedAction;
            }

            spec = spec.and((root, query, cb) ->
                    cb.equal(root.get("action"), dbAction));
        }
 
        Page<AuditLog> result = auditLogRepository.findAll(
            spec, PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt")));
 
        return ResponseEntity.ok(result.map(this::toDTO));
    }
}
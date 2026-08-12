package com.hospital.hms.controller;

import com.hospital.hms.dto.request.MedicalRecordRequestDTO;
import com.hospital.hms.dto.response.MedicalRecordResponseDTO;
import com.hospital.hms.model.MedicalRecord;
import com.hospital.hms.model.User;
import com.hospital.hms.repository.MedicalRecordRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.MedicalRecordService;
import com.hospital.hms.service.ai.AiVisitSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Medical Records",
        description = "Diagnosis and treatment records")
@Slf4j
@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private AiVisitSummaryService aiVisitSummaryService;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @GetMapping("/{id}")
    public ResponseEntity<MedicalRecordResponseDTO>
    getById(@PathVariable Long id) {
        return ResponseEntity.ok(
                medicalRecordService.getById(id));
    }

    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<MedicalRecordResponseDTO>
    getByAppointment(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(
                medicalRecordService
                        .getByAppointmentId(appointmentId));
    }

    @GetMapping("/patient/{patientId}/history")
    public ResponseEntity<List<MedicalRecordResponseDTO>>
    getPatientHistory(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                medicalRecordService
                        .getPatientHistory(patientId));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponseDTO>
    createRecord(
            @Valid @RequestBody
            MedicalRecordRequestDTO dto) {
        return ResponseEntity.ok(
                medicalRecordService.createRecord(dto));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponseDTO>
    updateRecord(@PathVariable Long id,
                 @Valid @RequestBody
                 MedicalRecordRequestDTO dto) {
        return ResponseEntity.ok(
                medicalRecordService.updateRecord(id, dto));
    }

    // ── Add this method to existing MedicalRecordController ──

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")        // ✅ Admin only
    public ResponseEntity<List<MedicalRecordResponseDTO>>
    getAllRecords() {
        return ResponseEntity.ok(
                medicalRecordService.getAllRecords());
    }

    @Operation(
        summary = "Get a plain-language explanation of a medical record",
        description = "Generates (and caches) an AI explanation of the " +
                      "diagnosis and treatment, written for the patient. " +
                      "Only the owning patient can request it."
    )
    @PostMapping("/{id}/explain")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<String> explainRecord(
            @PathVariable Long id,
            Authentication authentication) {
        log.info("Explain-record requested for record {}", id);

        MedicalRecord record = medicalRecordRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Record not found!"));

        User user = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found!"));

        if (!record.getAppointment().getPatient().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only view explanations for your own records!");
        }

        // Cache it — only call the AI once per record
        if (record.getPatientSummary() == null || record.getPatientSummary().isBlank()) {
            String summary = aiVisitSummaryService.summarize(record);
            record.setPatientSummary(summary);
            medicalRecordRepository.save(record);
        }

        return ResponseEntity.ok(record.getPatientSummary());
    }
}
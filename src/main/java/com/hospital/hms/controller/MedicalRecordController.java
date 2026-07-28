package com.hospital.hms.controller;

import com.hospital.hms.dto.request.MedicalRecordRequestDTO;
import com.hospital.hms.dto.response.MedicalRecordResponseDTO;
import com.hospital.hms.service.MedicalRecordService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Medical Records",
        description = "Diagnosis and treatment records")
@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

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
}
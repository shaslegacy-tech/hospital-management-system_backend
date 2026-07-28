package com.hospital.hms.controller;

import com.hospital.hms.dto.request.PrescriptionRequestDTO;
import com.hospital.hms.dto.response.PrescriptionResponseDTO;
import com.hospital.hms.service.PrescriptionService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Prescriptions",
        description = "Medicine prescriptions")
@RestController
@RequestMapping("/api/prescriptions")
public class PrescriptionController {

    @Autowired
    private PrescriptionService prescriptionService;

    @GetMapping("/record/{recordId}")
    public ResponseEntity<List<PrescriptionResponseDTO>>
    getByRecord(@PathVariable Long recordId) {
        return ResponseEntity.ok(
                prescriptionService.getByMedicalRecord(recordId));
    }

    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<PrescriptionResponseDTO>
    addPrescription(
            @Valid @RequestBody
            PrescriptionRequestDTO dto) {
        return ResponseEntity.ok(
                prescriptionService.addPrescription(dto));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<String> deletePrescription(
            @PathVariable Long id) {
        prescriptionService.deletePrescription(id);
        return ResponseEntity.ok(
                "Prescription deleted successfully");
    }
}
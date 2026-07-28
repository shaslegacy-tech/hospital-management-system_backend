package com.hospital.hms.controller;

import com.hospital.hms.dto.request.PatientRequestDTO;
import com.hospital.hms.dto.response.PatientResponseDTO;
import com.hospital.hms.model.enums.BloodGroup;
import com.hospital.hms.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Patients",
        description = "Manage patient profiles")
@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    // GET /api/patients — ADMIN, DOCTOR, RECEPTIONIST
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<Page<PatientResponseDTO>>
    getAllPatients(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                patientService.getAllPatients(page, size));
    }

    //GET-search
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR'," +
            "'RECEPTIONIST')")
    public ResponseEntity<Page<PatientResponseDTO>>
    searchPatients(
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false)
            BloodGroup bloodGroup,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                patientService.searchPatients(
                        name, email, bloodGroup, page, size));
    }

    // GET /api/patients/1
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO>
    getPatientById(@PathVariable Long id) {
        return ResponseEntity.ok(
                patientService.getPatientById(id));
    }

    // GET /api/patients/user/1
    @GetMapping("/user/{userId}")
    public ResponseEntity<PatientResponseDTO>
    getPatientByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                patientService.getPatientByUserId(userId));
    }

    // POST /api/patients
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO> createPatient(
            @Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.ok(
                patientService.createPatient(dto));
    }

    // PUT /api/patients/1
    @PutMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<PatientResponseDTO> updatePatient(
            @PathVariable Long id,
            @Valid @RequestBody PatientRequestDTO dto) {
        return ResponseEntity.ok(
                patientService.updatePatient(id, dto));
    }

    // DELETE /api/patients/1 — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deletePatient(
            @PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.ok(
                "Patient deleted successfully");
    }
    @Operation(
            summary = "Create your own patient profile",
            description = "A newly registered patient creates their medical profile")
    @PostMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponseDTO> createOwnProfile(
            @Valid @RequestBody PatientRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(patientService.createOwnProfile(
                        dto, authentication.getName()));
    }

    @Operation(
            summary = "Update your own patient profile",
            description = "A patient updates their medical details")
    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponseDTO> updateOwnProfile(
            @Valid @RequestBody PatientRequestDTO dto,
            Authentication authentication) {
        return ResponseEntity.ok(
                patientService.updateOwnProfile(
                        dto, authentication.getName()));
    }

    @Operation(
            summary = "Get your own patient profile",
            description = "A patient views their own medical profile")
    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponseDTO> getOwnProfile(
            Authentication authentication) {
        return ResponseEntity.ok(
                patientService.getOwnProfile(
                        authentication.getName()));
    }

}
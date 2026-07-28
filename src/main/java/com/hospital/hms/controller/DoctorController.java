package com.hospital.hms.controller;

import com.hospital.hms.dto.request.DoctorRequestDTO;
import com.hospital.hms.dto.response.DoctorResponseDTO;
import com.hospital.hms.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Doctors",
        description = "Manage doctor profiles and availability")
@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    // GET /api/doctors
    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>>
    getAllDoctors() {
        return ResponseEntity.ok(
                doctorService.getAllDoctors());
    }

    //GET-Search Functionality

    @Operation(
            summary = "Search doctors with filters",
            description = "Search doctors by name, " +
                    "specialization, department, " +
                    "experience, availability, and fee. " +
                    "All filters are optional."
    )
    @GetMapping("/search")
    public ResponseEntity<Page<DoctorResponseDTO>>
    searchDoctors(
            @RequestParam(required = false) String name,
            @RequestParam(required = false)
            String specialization,
            @RequestParam(required = false)
            Long departmentId,
            @RequestParam(required = false)
            Integer minExperience,
            @RequestParam(required = false)
            Boolean available,
            @RequestParam(required = false) Double maxFee,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                doctorService.searchDoctors(name, specialization,
                        departmentId, minExperience, available,
                        maxFee, page, size));
    }
    // GET /api/doctors/available
    @GetMapping("/available")
    public ResponseEntity<List<DoctorResponseDTO>>
    getAvailableDoctors() {
        return ResponseEntity.ok(
                doctorService.getAvailableDoctors());
    }

    // GET /api/doctors/1
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(
                doctorService.getDoctorById(id));
    }

    // GET /api/doctors/department/1
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(
                doctorService.getDoctorsByDepartment(
                        departmentId));
    }

    // POST /api/doctors — ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(
                doctorService.createDoctor(dto));
    }

    // PUT /api/doctors/1 — ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(
                doctorService.updateDoctor(id, dto));
    }

    // PUT /api/doctors/1/availability — ADMIN, DOCTOR
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<DoctorResponseDTO>
    toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(
                doctorService.toggleAvailability(id));
    }

    // DELETE /api/doctors/1 — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDoctor(
            @PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(
                "Doctor profile deleted successfully");
    }

    // GET /api/doctors/user/1
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT','RECEPTIONIST')")
    public ResponseEntity<DoctorResponseDTO> getDoctorByUserId(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                doctorService.getDoctorByUserId(userId));
    }

}
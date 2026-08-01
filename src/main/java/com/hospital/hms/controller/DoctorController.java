package com.hospital.hms.controller;

import com.hospital.hms.dto.request.DoctorOnboardRequestDTO;
import com.hospital.hms.dto.request.DoctorRequestDTO;
import com.hospital.hms.dto.response.DoctorResponseDTO;
import com.hospital.hms.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
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

    // GET /api/doctors/search
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
                doctorService.searchDoctors(
                        name, specialization, departmentId,
                        minExperience, available,
                        maxFee, page, size));
    }

    // GET /api/doctors/available
    @GetMapping("/available")
    public ResponseEntity<List<DoctorResponseDTO>>
    getAvailableDoctors() {
        return ResponseEntity.ok(
                doctorService.getAvailableDoctors());
    }

    // GET /api/doctors/{id}
    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorById(@PathVariable Long id) {
        return ResponseEntity.ok(
                doctorService.getDoctorById(id));
    }

    // GET /api/doctors/department/{departmentId}
    @GetMapping("/department/{departmentId}")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByDepartment(
            @PathVariable Long departmentId) {
        return ResponseEntity.ok(
                doctorService.getDoctorsByDepartment(
                        departmentId));
    }

    // GET /api/doctors/user/{userId}
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR'," +
            "'PATIENT','RECEPTIONIST')")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorByUserId(@PathVariable Long userId) {
        return ResponseEntity.ok(
                doctorService.getDoctorByUserId(userId));
    }

    // ✅ NEW - GET /api/doctors/{id}/available-slots
    @Operation(
            summary = "Get available time slots for a " +
                    "doctor on a given date",
            description = "Generates slots between the " +
                    "doctor's working hours at their " +
                    "configured interval, excluding " +
                    "already-booked slots and (for today)" +
                    " any slot in the past."
    )
    @GetMapping("/{id}/available-slots")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR'," +
            "'PATIENT','RECEPTIONIST')")
    public ResponseEntity<List<String>> getAvailableSlots(
            @PathVariable Long id,
            @RequestParam String date) {
        log.info("Fetching available slots for doctor {}" +
                " on {}", id, date);
        return ResponseEntity.ok(
                doctorService.getAvailableSlots(id, date));
    }

    // POST /api/doctors — ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> createDoctor(
            @Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(
                doctorService.createDoctor(dto));
    }

    // POST /api/doctors/onboard — ADMIN only
    @Operation(
            summary = "Onboard a new doctor",
            description = "Admin creates both the login " +
                    "account and the doctor profile in a " +
                    "single step — no separate " +
                    "registration needed."
    )
    @PostMapping("/onboard")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> onboardDoctor(
            @Valid @RequestBody DoctorOnboardRequestDTO dto) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(doctorService.onboardDoctor(dto));
    }

    // PUT /api/doctors/{id} — ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
            @Valid @RequestBody DoctorRequestDTO dto) {
        return ResponseEntity.ok(
                doctorService.updateDoctor(id, dto));
    }

    // PUT /api/doctors/{id}/availability — ADMIN, DOCTOR
    @PutMapping("/{id}/availability")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<DoctorResponseDTO>
    toggleAvailability(@PathVariable Long id) {
        return ResponseEntity.ok(
                doctorService.toggleAvailability(id));
    }

    // DELETE /api/doctors/{id} — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDoctor(
            @PathVariable Long id) {
        doctorService.deleteDoctor(id);
        return ResponseEntity.ok(
                "Doctor profile deleted successfully");
    }
}

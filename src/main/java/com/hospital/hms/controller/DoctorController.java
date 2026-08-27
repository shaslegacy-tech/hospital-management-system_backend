package com.hospital.hms.controller;

import com.hospital.hms.dto.request.DoctorOnboardRequestDTO;
import com.hospital.hms.dto.request.DoctorRequestDTO;
import com.hospital.hms.dto.response.DoctorResponseDTO;
import com.hospital.hms.service.DoctorService;
import com.hospital.hms.service.HospitalContextService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
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

    @Autowired private HospitalContextService hospitalContextService;

    // GET /api/doctors
    @GetMapping
    public ResponseEntity<List<DoctorResponseDTO>>
        getAllDoctors(
                        @RequestParam(required = false) Long hospitalId,
                        Authentication authentication) {
                Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
                if (targetHospitalId == null) {
                        return ResponseEntity.ok(List.of());
                }
        return ResponseEntity.ok(
                                doctorService.getAllDoctors(targetHospitalId));
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
            @RequestParam(required = false) String gender,
                        @RequestParam(required = false) Long hospitalId,
            @RequestParam(defaultValue = "0") int page,
                        @RequestParam(defaultValue = "10") int size,
                        Authentication authentication) {

                Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
                if (targetHospitalId == null) {
                        return ResponseEntity.ok(Page.empty());
                }

        return ResponseEntity.ok(
                doctorService.searchDoctors(
                        name, specialization, departmentId,
                        minExperience, available,
                                                maxFee, gender, targetHospitalId, page, size));
    }

        private Long resolveHospitalId(
                        Long hospitalId, Authentication authentication) {
                if (hospitalId != null) {
                        return hospitalId;
                }
                try {
                        return hospitalContextService
                                        .getCurrentUserHospital(authentication).getId();
                } catch (Exception ignored) {
                        return null;
                }
        }

    // GET /api/doctors/available
    @GetMapping("/available")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT','RECEPTIONIST')")
    public ResponseEntity<List<DoctorResponseDTO>>
    getAvailableDoctors(
            @RequestParam(required = false) Long hospitalId,
            Authentication authentication) {
        Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
        if (targetHospitalId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(
                doctorService.getAvailableDoctors(targetHospitalId));
    }

    // GET /api/doctors/{id}
    @GetMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT','RECEPTIONIST')")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorById(
            @PathVariable Long id,
            @RequestParam(required = false) Long hospitalId,
            Authentication authentication) {
        Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
        if (targetHospitalId == null) {
            throw new RuntimeException("Unable to determine hospital context");
        }
        return ResponseEntity.ok(
                doctorService.getDoctorById(id, targetHospitalId));
    }

    // GET /api/doctors/department/{departmentId}
    // ✅ Added optional ?available=true/false param
    @GetMapping("/department/{departmentId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT','RECEPTIONIST')")
    public ResponseEntity<List<DoctorResponseDTO>>
    getDoctorsByDepartment(
            @PathVariable Long departmentId,
            @RequestParam(required = false) Boolean available,
            @RequestParam(required = false) Long hospitalId,
            Authentication authentication) {
        Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
        if (targetHospitalId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(
                doctorService.getDoctorsByDepartment(
                        departmentId, available, targetHospitalId));
    }


    // GET /api/doctors/user/{userId}
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR'," +
            "'PATIENT','RECEPTIONIST')")
    public ResponseEntity<DoctorResponseDTO>
    getDoctorByUserId(
            @PathVariable Long userId,
            @RequestParam(required = false) Long hospitalId,
            Authentication authentication) {
        Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
        if (targetHospitalId == null) {
            throw new RuntimeException("Unable to determine hospital context");
        }
        return ResponseEntity.ok(
                doctorService.getDoctorByUserId(userId, targetHospitalId));
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
            @RequestParam String date,
            @RequestParam(required = false) Long hospitalId,
            Authentication authentication) {
        Long targetHospitalId = resolveHospitalId(hospitalId, authentication);
        if (targetHospitalId == null) {
            return ResponseEntity.ok(List.of());
        }
        log.info("Fetching available slots for doctor {}" +
                " on {} hospital: {}", id, date, targetHospitalId);
        return ResponseEntity.ok(
                doctorService.getAvailableSlots(id, date, targetHospitalId));
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
                        @Valid @RequestBody DoctorOnboardRequestDTO dto,
                        Authentication authentication) {
        return ResponseEntity
                .status(HttpStatus.CREATED)
                                .body(doctorService.onboardDoctor(dto, authentication));
    }

    // PUT /api/doctors/{id} — ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DoctorResponseDTO> updateDoctor(
            @PathVariable Long id,
                        @Valid @RequestBody DoctorRequestDTO dto,
                        Authentication authentication) {
        return ResponseEntity.ok(
                                doctorService.updateDoctor(id, dto, authentication));
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
                        @PathVariable Long id,
                        Authentication authentication) {
                doctorService.deleteDoctor(id, authentication);
        return ResponseEntity.ok(
                "Doctor profile deleted successfully");
    }
}

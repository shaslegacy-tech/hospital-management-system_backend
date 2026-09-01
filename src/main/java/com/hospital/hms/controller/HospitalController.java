package com.hospital.hms.controller;

import com.hospital.hms.dto.request.HospitalRegisterRequestDTO;
import com.hospital.hms.dto.response.HospitalResponseDTO;
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.HospitalStatus;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.repository.HospitalRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.GeocodingService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Hospitals", description = "Hospital tenant registration and platform management")
@Slf4j
@RestController
@RequestMapping("/api/hospitals")
public class HospitalController {

    @Autowired
    private HospitalRepository hospitalRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    @Lazy
    private PasswordEncoder passwordEncoder;
    @Autowired
    private GeocodingService geocodingService;

    private HospitalResponseDTO toDTO(Hospital h) {
        return new HospitalResponseDTO(
                h.getId(), h.getName(), h.getAddress(), h.getCity(), h.getState(),
                h.getPincode(), h.getLatitude(), h.getLongitude(), h.getContactPhone(),
                h.getContactEmail(), h.getDescription(), h.getLogoUrl(),
                h.getStatus().toString());
    }

    @Operation(summary = "Register a new hospital", description = "Public endpoint. Creates the hospital (status " +
            "PENDING) and its first admin account together. " +
            "Requires SUPER_ADMIN approval before the hospital " +
            "is visible to patients or the admin can fully use it.")
    @PostMapping("/register")
    public ResponseEntity<HospitalResponseDTO> registerHospital(
            @Valid @RequestBody HospitalRegisterRequestDTO dto) {

        log.info("New hospital registration: {}", dto.getHospitalName());

        if (userRepository.findByEmail(dto.getAdminEmail()).isPresent()) {
            throw new RuntimeException(
                    "An account with this email already exists!");
        }

        Hospital hospital = new Hospital();

        hospital.setName(dto.getHospitalName());
        hospital.setAddress(dto.getAddress());
        hospital.setCity(dto.getCity());
        hospital.setState(dto.getState());
        hospital.setPincode(dto.getPincode());
        hospital.setDescription(dto.getDescription());
        hospital.setContactPhone(dto.getContactPhone());
        hospital.setContactEmail(dto.getAdminEmail());
        hospital.setStatus(HospitalStatus.PENDING);

        geocodingService
                .geocode(
                        dto.getAddress(),
                        dto.getCity(),
                        dto.getState(),
                        dto.getPincode())
                .ifPresent(coords -> {
                    hospital.setLatitude(coords.latitude);
                    hospital.setLongitude(coords.longitude);
                });

        // Save hospital
        Hospital savedHospital = hospitalRepository.save(hospital);

        // Create hospital admin
        User admin = new User();

        admin.setName(dto.getAdminName());
        admin.setEmail(dto.getAdminEmail());
        admin.setPassword(
                passwordEncoder.encode(dto.getAdminPassword()));
        admin.setPhone(dto.getAdminPhone());
        admin.setRole(Role.ADMIN);
        admin.setHospital(savedHospital);

        userRepository.save(admin);

        log.info(
                "Hospital {} registered, pending approval",
                savedHospital.getId());

        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(savedHospital));
    }

    @Operation(summary = "List approved hospitals — public, used for patient discovery")
    @GetMapping
    public ResponseEntity<List<HospitalResponseDTO>> getApprovedHospitals() {
        return ResponseEntity.ok(
                hospitalRepository.findByStatus(HospitalStatus.APPROVED)
                        .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Operation(summary = "Get a single hospital's public details")
    @GetMapping("/{id}")
    public ResponseEntity<HospitalResponseDTO> getHospital(@PathVariable Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found!"));
        return ResponseEntity.ok(toDTO(hospital));
    }

    // ============ Platform (SUPER_ADMIN) endpoints ============

    @Operation(summary = "List hospitals pending approval — SUPER_ADMIN only")
    @GetMapping("/pending")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<HospitalResponseDTO>> getPendingHospitals() {
        return ResponseEntity.ok(
                hospitalRepository.findByStatus(HospitalStatus.PENDING)
                        .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @Operation(summary = "List ALL hospitals regardless of status — SUPER_ADMIN only")
    @GetMapping("/all")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<List<HospitalResponseDTO>> getAllHospitals() {
        return ResponseEntity.ok(
                hospitalRepository.findAll()
                        .stream().map(this::toDTO).collect(Collectors.toList()));
    }

    @PutMapping("/{id}/approve")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HospitalResponseDTO> approveHospital(@PathVariable Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found!"));
        hospital.setStatus(HospitalStatus.APPROVED);
        log.info("Hospital {} approved", id);
        return ResponseEntity.ok(toDTO(hospitalRepository.save(hospital)));
    }

    @PutMapping("/{id}/reject")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HospitalResponseDTO> rejectHospital(@PathVariable Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found!"));
        hospital.setStatus(HospitalStatus.REJECTED);
        log.info("Hospital {} rejected", id);
        return ResponseEntity.ok(toDTO(hospitalRepository.save(hospital)));
    }

    @PutMapping("/{id}/suspend")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    public ResponseEntity<HospitalResponseDTO> suspendHospital(@PathVariable Long id) {
        Hospital hospital = hospitalRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Hospital not found!"));
        hospital.setStatus(HospitalStatus.SUSPENDED);
        log.warn("Hospital {} suspended", id);
        return ResponseEntity.ok(toDTO(hospitalRepository.save(hospital)));
    }
}

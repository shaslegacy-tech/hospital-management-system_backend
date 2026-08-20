package com.hospital.hms.controller;

import com.hospital.hms.dto.request.AddCaregiverRequestDTO;
import com.hospital.hms.dto.response.CaregiverLinkResponseDTO;
import com.hospital.hms.model.CaregiverLink;
import com.hospital.hms.model.Patient;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.repository.CaregiverLinkRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.UserRepository;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Caregivers",
        description = "Family/caregiver access to patient data"
)
@Slf4j
@RestController
@RequestMapping("/api/caregivers")
@RequiredArgsConstructor
public class CaregiverController {

    private final CaregiverLinkRepository caregiverLinkRepository;

    private final PatientRepository patientRepository;

    private final UserRepository userRepository;


    // ============================================================
    // Helper: Get currently authenticated user
    // ============================================================

    private User currentUser(Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found!")
                );
    }


    // ============================================================
    // Helper: Convert entity to response DTO
    // ============================================================

    private CaregiverLinkResponseDTO toDTO(
            CaregiverLink link
    ) {

        return new CaregiverLinkResponseDTO(
                link.getId(),

                link.getPatient().getId(),

                link.getPatient()
                        .getUser()
                        .getName(),

                link.getCaregiver()
                        .getName(),

                link.getCaregiver()
                        .getEmail(),

                link.getRelationship()
        );
    }


    // ============================================================
    // 1. PATIENT → ADD CAREGIVER
    // ============================================================

    @Operation(
            summary = "Add a caregiver",
            description =
                    "Allows a patient to give another registered "
                    + "caregiver access to their patient data."
    )
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<CaregiverLinkResponseDTO> addCaregiver(
            @Valid @RequestBody AddCaregiverRequestDTO dto,
            Authentication authentication
    ) {

        log.info(
                "Adding caregiver with email: {}",
                dto.getCaregiverEmail()
        );

        // Logged-in patient
        User patientUser = currentUser(authentication);


        // Find patient profile
        Patient patient = patientRepository
                .findByUserId(patientUser.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Your patient profile isn't set up yet!"
                        )
                );


        // Find caregiver account
        User caregiverUser = userRepository
                .findByEmail(dto.getCaregiverEmail())
                .orElseThrow(() ->
                        new RuntimeException(
                                "No account found with that email. "
                                + "The caregiver needs to register first."
                        )
                );


        // Make sure selected user is actually a caregiver
        if (caregiverUser.getRole() != Role.CAREGIVER) {

            throw new IllegalArgumentException(
                    "The selected user is not registered as a caregiver."
            );
        }


        // Patient cannot add himself/herself
        if (caregiverUser.getId()
                .equals(patientUser.getId())) {

            throw new IllegalArgumentException(
                    "You can't add yourself as your own caregiver!"
            );
        }


        // Prevent duplicate active relationship
        boolean alreadyLinked =
                caregiverLinkRepository
                        .existsByCaregiverIdAndPatientIdAndActiveTrue(
                                caregiverUser.getId(),
                                patient.getId()
                        );

        if (alreadyLinked) {

            throw new IllegalArgumentException(
                    "This person already has caregiver access!"
            );
        }


        // Check if an old/revoked link exists
        CaregiverLink existingLink =
                caregiverLinkRepository
                        .findByCaregiverIdAndPatientId(
                                caregiverUser.getId(),
                                patient.getId()
                        )
                        .orElse(null);


        if (existingLink != null) {

            // Re-enable previous relationship
            existingLink.setActive(true);
            existingLink.setRelationship(
                    dto.getRelationship()
            );

            log.info(
                    "Caregiver access reactivated: caregiver={} patient={}",
                    caregiverUser.getId(),
                    patient.getId()
            );

            return ResponseEntity
                    .status(HttpStatus.CREATED)
                    .body(
                            toDTO(
                                    caregiverLinkRepository.save(
                                            existingLink
                                    )
                            )
                    );
        }


        // Create new relationship
        CaregiverLink link = new CaregiverLink();

        link.setCaregiver(caregiverUser);
        link.setPatient(patient);
        link.setRelationship(dto.getRelationship());
        link.setActive(true);


        CaregiverLink saved =
                caregiverLinkRepository.save(link);


        log.info(
                "Caregiver link created: caregiver={} patient={}",
                caregiverUser.getId(),
                patient.getId()
        );


        return ResponseEntity
                .status(HttpStatus.CREATED)
                .body(toDTO(saved));
    }


    // ============================================================
    // 2. PATIENT → VIEW THEIR CAREGIVERS
    // ============================================================

    @Operation(
            summary = "Get my caregivers",
            description =
                    "Returns all active caregivers who have "
                    + "access to the logged-in patient's data."
    )
    @GetMapping("/my-caregivers")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<CaregiverLinkResponseDTO>>
    getMyCaregivers(
            Authentication authentication
    ) {

        User patientUser = currentUser(authentication);


        Patient patient = patientRepository
                .findByUserId(patientUser.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient profile not found!"
                        )
                );


        List<CaregiverLinkResponseDTO> caregivers =
                caregiverLinkRepository
                        .findByPatientIdAndActiveTrue(
                                patient.getId()
                        )
                        .stream()
                        .map(this::toDTO)
                        .toList();


        return ResponseEntity.ok(caregivers);
    }


    // ============================================================
    // 3. CAREGIVER → VIEW MANAGED PATIENTS
    // ============================================================

    @Operation(
            summary = "Get managed patients",
            description =
                    "Returns all patients whose data the "
                    + "logged-in caregiver is authorized to access."
    )
    @GetMapping("/my-managed-patients")
    @PreAuthorize("hasRole('CAREGIVER')")
    public ResponseEntity<List<CaregiverLinkResponseDTO>>
    getMyManagedPatients(
            Authentication authentication
    ) {

        User caregiverUser =
                currentUser(authentication);


        List<CaregiverLinkResponseDTO> patients =
                caregiverLinkRepository
                        .findByCaregiverIdAndActiveTrue(
                                caregiverUser.getId()
                        )
                        .stream()
                        .map(this::toDTO)
                        .toList();


        return ResponseEntity.ok(patients);
    }


    // ============================================================
    // 4. REMOVE / REVOKE CAREGIVER ACCESS
    // ============================================================

    @Operation(
            summary = "Remove caregiver access",
            description =
                    "Patient can revoke caregiver access. "
                    + "Caregiver can also remove themselves."
    )
    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('PATIENT','CAREGIVER')"
    )
    public ResponseEntity<String> removeCaregiver(
            @PathVariable Long id,
            Authentication authentication
    ) {

        User user = currentUser(authentication);


        CaregiverLink link =
                caregiverLinkRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Caregiver link not found!"
                                )
                        );


        // Patient who owns the relationship
        boolean isPatientOwner =
                link.getPatient()
                        .getUser()
                        .getId()
                        .equals(user.getId());


        // Caregiver themselves
        boolean isCaregiverThemself =
                link.getCaregiver()
                        .getId()
                        .equals(user.getId());


        if (!isPatientOwner && !isCaregiverThemself) {

            throw new AccessDeniedException(
                    "You don't have permission to remove this caregiver link."
            );
        }


        // Instead of deleting the row,
        // revoke the access.
        link.setActive(false);

        caregiverLinkRepository.save(link);


        log.info(
                "Caregiver access revoked: linkId={} by user={}",
                id,
                user.getId()
        );


        return ResponseEntity.ok(
                "Caregiver access removed successfully"
        );
    }
}
package com.hospital.hms.controller;

import com.hospital.hms.dto.response.PatientFileResponseDTO;
import com.hospital.hms.model.Patient;
import com.hospital.hms.model.PatientFile;
import com.hospital.hms.model.User;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.CaregiverAccessService;
import com.hospital.hms.service.PatientFileService;
import com.hospital.hms.service.file.FileStorageService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;

import org.springframework.http.*;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(
        name = "Files",
        description =
                "Upload and manage patient files "
                + "(reports, X-rays, prescriptions)"
)
@RestController
@RequestMapping("/api/files")
public class PatientFileController {

    @Autowired
    private PatientFileService patientFileService;

    @Autowired
    private FileStorageService fileStorageService;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaregiverAccessService caregiverAccessService;


    // ============================================================
    // Helper methods
    // ============================================================

    private User currentUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found!"
                        )
                );
    }


    private Patient getPatient(
            Long patientId) {

        return patientRepository
                .findById(patientId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found!"
                        )
                );
    }


    private void validatePatientAccess(
            Authentication authentication,
            Patient patient) {

        User user =
                currentUser(authentication);

        caregiverAccessService.validateAccess(
                user.getId(),
                patient.getUser().getId(),
                patient.getId()
        );
    }


    // ============================================================
    // GET files for a patient
    // ============================================================

    @GetMapping("/patient/{patientId}")
    @PreAuthorize(
            "hasAnyRole("
                    + "'ADMIN',"
                    + "'DOCTOR',"
                    + "'RECEPTIONIST',"
                    + "'PATIENT',"
                    + ")"
    )
    public ResponseEntity<List<PatientFileResponseDTO>>
    getPatientFiles(
            @PathVariable Long patientId,
            Authentication authentication) {

        User user =
                currentUser(authentication);

        String role =
                user.getRole().name();

        if (role.equals("PATIENT")) {

            Patient patient =
                    getPatient(patientId);

            validatePatientAccess(
                    authentication,
                    patient
            );
        }

        return ResponseEntity.ok(
                patientFileService
                        .getPatientFiles(patientId)
        );
    }


    // ============================================================
    // GET files by type
    // ============================================================

    @GetMapping(
            "/patient/{patientId}/type/{fileType}"
    )
    @PreAuthorize(
            "hasAnyRole("
                    + "'ADMIN',"
                    + "'DOCTOR',"
                    + "'RECEPTIONIST',"
                    + "'PATIENT'"
                    + ")"
    )
    public ResponseEntity<List<PatientFileResponseDTO>>
    getFilesByType(
            @PathVariable Long patientId,
            @PathVariable String fileType,
            Authentication authentication) {

        User user =
                currentUser(authentication);

        String role =
                user.getRole().name();

        if (role.equals("PATIENT")
                || role.equals("CAREGIVER")) {

            Patient patient =
                    getPatient(patientId);

            validatePatientAccess(
                    authentication,
                    patient
            );
        }

        return ResponseEntity.ok(
                patientFileService.getFilesByType(
                        patientId,
                        fileType
                )
        );
    }


    // ============================================================
    // POST - upload file
    // ============================================================

    @Operation(
            summary = "Upload patient file",
            description =
                    "Upload PDF or image file "
                    + "(reports, X-rays, prescriptions). "
                    + "Max size 10MB."
    )
    @PostMapping(
            consumes = "multipart/form-data"
    )
    @PreAuthorize(
            "hasAnyRole("
                    + "'ADMIN',"
                    + "'DOCTOR',"
                    + "'RECEPTIONIST',"
                    + "'PATIENT'"
                    + ")"
    )
    public ResponseEntity<PatientFileResponseDTO>
    uploadFile(
            @RequestParam Long patientId,
            @RequestParam MultipartFile file,
            @RequestParam String fileType,
            @RequestParam(required = false)
            String description,
            Authentication authentication) {

        User user =
                currentUser(authentication);

        String role =
                user.getRole().name();

        if (role.equals("PATIENT")
                || role.equals("CAREGIVER")) {

            Patient patient =
                    getPatient(patientId);

            validatePatientAccess(
                    authentication,
                    patient
            );
        }

        return ResponseEntity.ok(
                patientFileService.uploadFile(
                        patientId,
                        file,
                        fileType,
                        description
                )
        );
    }


    // ============================================================
    // GET - download file
    // ============================================================

    @GetMapping("/download/{fileName}")
    @PreAuthorize(
            "hasAnyRole("
                    + "'ADMIN',"
                    + "'DOCTOR',"
                    + "'RECEPTIONIST',"
                    + "'PATIENT'"
                    + ")"
    )
    public ResponseEntity<Resource>
    downloadFile(
            @PathVariable String fileName,
            Authentication authentication) {

        PatientFile file =
                patientFileService
                        .getFileEntityByFileName(
                                fileName
                        );

        User user =
                currentUser(authentication);

        String role =
                user.getRole().name();

        if (role.equals("PATIENT")
                || role.equals("CAREGIVER")) {

            validatePatientAccess(
                    authentication,
                    file.getPatient()
            );
        }

        Resource resource =
                fileStorageService
                        .loadFile(fileName);

        MediaType mediaType;

        try {

            mediaType =
                    file.getContentType() != null
                            ? MediaType.parseMediaType(
                                    file.getContentType()
                            )
                            : MediaType.APPLICATION_OCTET_STREAM;

        } catch (Exception e) {

            mediaType =
                    MediaType.APPLICATION_OCTET_STREAM;
        }

        return ResponseEntity.ok()
                .contentType(mediaType)
                .header(
                        HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                file.getOriginalFileName() +
                                "\""
                )
                .body(resource);
    }


    // ============================================================
    // DELETE file
    // ADMIN, DOCTOR only
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','DOCTOR')"
    )
    public ResponseEntity<String>
    deleteFile(
            @PathVariable Long id) {

        patientFileService.deleteFile(id);

        return ResponseEntity.ok(
                "File deleted successfully"
        );
    }
}
package com.hospital.hms.controller;

import com.hospital.hms.dto.response.PatientFileResponseDTO;
import com.hospital.hms.service.PatientFileService;
import com.hospital.hms.service.file.FileStorageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Tag(name = "Files",
        description = "Upload and manage patient files " +
                "(reports, X-rays, prescriptions)")
@RestController
@RequestMapping("/api/files")
public class PatientFileController {

    @Autowired
    private PatientFileService patientFileService;

    @Autowired
    private FileStorageService fileStorageService;

    // GET files for a patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<PatientFileResponseDTO>>
    getPatientFiles(
            @PathVariable Long patientId) {
        return ResponseEntity.ok(
                patientFileService.getPatientFiles(
                        patientId));
    }

    // GET files by type
    @GetMapping("/patient/{patientId}/type/{fileType}")
    public ResponseEntity<List<PatientFileResponseDTO>>
    getFilesByType(
            @PathVariable Long patientId,
            @PathVariable String fileType) {
        return ResponseEntity.ok(
                patientFileService.getFilesByType(
                        patientId, fileType));
    }

    // POST - upload file
    @Operation(
            summary = "Upload patient file",
            description = "Upload PDF or image file " +
                    "(reports, X-rays, prescriptions). " +
                    "Max size 10MB."
    )
    @PostMapping(consumes = "multipart/form-data")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR'," +
            "'RECEPTIONIST')")
    public ResponseEntity<PatientFileResponseDTO>
    uploadFile(
            @RequestParam Long patientId,
            @RequestParam MultipartFile file,
            @RequestParam String fileType,
            @RequestParam(required = false)
            String description) {

        return ResponseEntity.ok(
                patientFileService.uploadFile(
                        patientId, file, fileType, description));
    }

    // GET - download file
    @GetMapping("/download/{fileName}")
    public ResponseEntity<Resource> downloadFile(
            @PathVariable String fileName) {

        Resource resource =
                fileStorageService.loadFile(fileName);

        return ResponseEntity.ok()
                .contentType(
                        MediaType.APPLICATION_OCTET_STREAM)
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" +
                                resource.getFilename() + "\"")
                .body(resource);
    }

    // DELETE file
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR')")
    public ResponseEntity<String> deleteFile(
            @PathVariable Long id) {
        patientFileService.deleteFile(id);
        return ResponseEntity.ok(
                "File deleted successfully");
    }
}
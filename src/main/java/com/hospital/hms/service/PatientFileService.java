package com.hospital.hms.service;

import com.hospital.hms.dto.response.PatientFileResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.file.FileStorageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PatientFileService {

    @Autowired
    private PatientFileRepository patientFileRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private FileStorageService fileStorageService;

    private PatientFileResponseDTO toDTO(
            PatientFile file) {
        return new PatientFileResponseDTO(
                file.getId(),
                file.getPatient().getId(),
                file.getPatient().getUser().getName(),
                file.getOriginalFileName(),
                file.getFileType(),
                file.getContentType(),
                file.getFileSize(),
                file.getDescription(),
                "/api/files/download/" + file.getFileName(),
                file.getCreatedAt()
        );
    }

    // GET all files for a patient
    public List<PatientFileResponseDTO> getPatientFiles(
            Long patientId) {
        log.info("Fetching files for patient: {}",
                patientId);
        return patientFileRepository
                .findByPatientId(patientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET files by type
    public List<PatientFileResponseDTO> getFilesByType(
            Long patientId, String type) {
        return patientFileRepository
                .findByPatientIdAndFileType(
                        patientId, type)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // POST - upload file
    public PatientFileResponseDTO uploadFile(
            Long patientId,
            MultipartFile file,
            String fileType,
            String description) {

        log.info("Uploading file for patient: {}",
                patientId);

        Patient patient = patientRepository
                .findById(patientId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found!"));

        String contentType = file.getContentType();
        String originalName = file.getOriginalFilename();

        log.info("Content-Type: {}, FileName: {}",
                contentType, originalName);

        // Validate by extension (more reliable than
        // content-type which varies by client)
        boolean isValidExtension = originalName != null &&
                (originalName.toLowerCase().endsWith(".pdf") ||
                        originalName.toLowerCase().endsWith(".png") ||
                        originalName.toLowerCase().endsWith(".jpg") ||
                        originalName.toLowerCase().endsWith(".jpeg"));

        if (!isValidExtension) {
            throw new RuntimeException(
                    "Only PDF and image files (jpg/png) " +
                            "are allowed!");
        }

        // Validate size
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new RuntimeException(
                    "File size must be under 10MB!");
        }

        String storedFileName =
                fileStorageService.storeFile(file);

        // Determine content type from extension if
        // browser didn't send correct one
        String finalContentType = contentType;
        if (contentType == null ||
                contentType.equals(
                        "application/octet-stream")) {
            finalContentType = originalName
                    .toLowerCase().endsWith(".pdf")
                    ? "application/pdf"
                    : "image/jpeg";
        }

        PatientFile patientFile = new PatientFile();
        patientFile.setPatient(patient);
        patientFile.setFileName(storedFileName);
        patientFile.setOriginalFileName(originalName);
        patientFile.setFileType(fileType);
        patientFile.setContentType(finalContentType);
        patientFile.setFilePath(storedFileName);
        patientFile.setFileSize(file.getSize());
        patientFile.setDescription(description);

        log.info("File uploaded successfully: {}",
                storedFileName);

        return toDTO(
                patientFileRepository.save(patientFile));
    }

    // DELETE file
    public void deleteFile(Long id) {
        log.warn("Deleting file: {}", id);

        PatientFile file = patientFileRepository
                .findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "File not found: " + id));

        fileStorageService.deleteFile(file.getFileName());
        patientFileRepository.delete(file);
    }

    // Get file entity for download (used by controller)
    public PatientFile getFileEntity(Long id) {
        return patientFileRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "File not found: " + id));
    }
}
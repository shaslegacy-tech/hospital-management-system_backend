package com.hospital.hms.service.file;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.MalformedURLException;
import java.nio.file.*;
import java.util.UUID;

@Slf4j
@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    // Store file on disk, return unique filename
    public String storeFile(MultipartFile file) {
        try {
            Path uploadPath = Paths.get(uploadDir);
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
            }

            // Generate unique filename to avoid collisions
            String originalName = file.getOriginalFilename();
            String extension = originalName.substring(
                    originalName.lastIndexOf("."));
            String uniqueFileName =
                    UUID.randomUUID().toString() + extension;

            Path targetPath =
                    uploadPath.resolve(uniqueFileName);
            Files.copy(file.getInputStream(), targetPath,
                    StandardCopyOption.REPLACE_EXISTING);

            log.info("File stored: {}", uniqueFileName);
            return uniqueFileName;

        } catch (IOException e) {
            log.error("Failed to store file: {}",
                    e.getMessage());
            throw new RuntimeException(
                    "Failed to store file: " + e.getMessage());
        }
    }

    // Load file as downloadable resource
    public Resource loadFile(String fileName) {
        try {
            Path filePath =
                    Paths.get(uploadDir).resolve(fileName);
            Resource resource =
                    new UrlResource(filePath.toUri());

            if (resource.exists()) {
                return resource;
            } else {
                throw new RuntimeException(
                        "File not found: " + fileName);
            }
        } catch (MalformedURLException e) {
            throw new RuntimeException(
                    "File not found: " + fileName);
        }
    }

    // Delete file from disk
    public void deleteFile(String fileName) {
        try {
            Path filePath =
                    Paths.get(uploadDir).resolve(fileName);
            Files.deleteIfExists(filePath);
            log.info("File deleted: {}", fileName);
        } catch (IOException e) {
            log.error("Failed to delete file: {}",
                    e.getMessage());
        }
    }
}
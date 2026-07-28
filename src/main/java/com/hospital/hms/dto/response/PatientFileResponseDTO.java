package com.hospital.hms.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientFileResponseDTO {
    private Long id;
    private Long patientId;
    private String patientName;
    private String originalFileName;
    private String fileType;
    private String contentType;
    private Long fileSize;
    private String description;
    private String downloadUrl;
    private LocalDateTime createdAt;
}
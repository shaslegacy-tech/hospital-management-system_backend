package com.hospital.hms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionRequestDTO {

    @NotNull(message = "Medical record ID is required")
    private Long medicalRecordId;

    @NotBlank(message = "Medicine name is required")
    private String medicineName;

    @NotBlank(message = "Dosage is required")
    private String dosage;

    @NotBlank(message = "Duration is required")
    private String duration;

    private String instructions;
}
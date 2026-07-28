package com.hospital.hms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience cannot be negative")
    private Integer experienceYears;

    @NotNull(message = "Consultation fee is required")
    @Min(value = 0, message = "Fee cannot be negative")
    private Double consultationFee;

    private String bio;
}
package com.hospital.hms.dto.request;

import jakarta.validation.constraints.*;
import lombok.Data;

@Data
public class DoctorOnboardRequestDTO {
    @NotBlank
    private String name;

    @NotBlank @Email
    private String email;

    @NotBlank @Size(min = 6, message = "Password must be at least 6 characters")
    private String password;

    @NotBlank
    private String phone;

    @NotNull
    private Long departmentId;

    @NotBlank
    private String specialization;

    @NotNull
    private Integer experienceYears;

    @NotNull
    private Double consultationFee;

    private String bio;
}
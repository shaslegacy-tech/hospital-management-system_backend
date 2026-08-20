package com.hospital.hms.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AddCaregiverRequestDTO {

    @NotBlank(message = "Caregiver email is required")
    @Email(message = "Please provide a valid caregiver email")
    private String caregiverEmail;

    @NotBlank(message = "Relationship is required")
    private String relationship;
}
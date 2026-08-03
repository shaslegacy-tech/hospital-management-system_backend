package com.hospital.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ApprovePatientRequestDTO {

    @NotBlank(message = "Date of birth is required")
    private String dateOfBirth;

    @NotBlank(message = "Blood group is required")
    private String bloodGroup;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    @NotBlank(message = "Emergency contact is required")
    private String emergencyContact;

    private String medicalHistory;
}

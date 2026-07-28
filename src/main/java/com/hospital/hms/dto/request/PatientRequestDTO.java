package com.hospital.hms.dto.request;

import com.hospital.hms.model.enums.BloodGroup;
import jakarta.validation.constraints.*;
import lombok.*;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientRequestDTO {

    @NotNull(message = "User ID is required")
    private Long userId;

    @NotNull(message = "Date of birth is required")
    private LocalDate dateOfBirth;

    @NotNull(message = "Blood group is required")
    private BloodGroup bloodGroup;

    @NotBlank(message = "Address is required")
    private String address;

    @NotBlank(message = "Emergency contact is required")
    private String emergencyContact;

    @NotBlank(message = "Emergency contact name is required")
    private String emergencyContactName;

    private String medicalHistory;
}
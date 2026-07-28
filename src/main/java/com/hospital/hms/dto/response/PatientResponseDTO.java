package com.hospital.hms.dto.response;

import com.hospital.hms.model.enums.BloodGroup;
import lombok.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientResponseDTO {
    private Long id;
    private Long userId;
    private String patientName;
    private String email;
    private String phone;
    private LocalDate dateOfBirth;
    private BloodGroup bloodGroup;
    private String address;
    private String emergencyContact;
    private String emergencyContactName;
    private String medicalHistory;
    private LocalDateTime createdAt;
}
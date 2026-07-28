package com.hospital.hms.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponseDTO {
    private Long id;
    private Long userId;
    private String doctorName;
    private String email;
    private String phone;
    private String departmentName;
    private String specialization;
    private Integer experienceYears;
    private Double consultationFee;
    private String bio;
    private boolean available;
    private LocalDateTime createdAt;
}
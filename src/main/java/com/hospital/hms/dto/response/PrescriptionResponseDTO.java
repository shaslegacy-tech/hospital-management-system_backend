package com.hospital.hms.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PrescriptionResponseDTO {
    private Long id;
    private String medicineName;
    private String dosage;
    private String duration;
    private String instructions;
    private LocalDateTime createdAt;
}
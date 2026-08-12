package com.hospital.hms.dto.response;

import lombok.*;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecordResponseDTO {
    private Long id;
    private Long appointmentId;
    private String patientName;
    private String doctorName;
    private String diagnosis;
    private String treatment;
    private String notes;
    private List<PrescriptionResponseDTO> prescriptions;
    private LocalDateTime createdAt;
    private String patientSummary;
}
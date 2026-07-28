package com.hospital.hms.dto.response;

import com.hospital.hms.model.enums.BillStatus;
import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillResponseDTO {
    private Long id;
    private Long appointmentId;
    private String patientName;
    private String doctorName;
    private String departmentName;
    private Double consultationFee;
    private Double additionalCharges;
    private Double totalAmount;
    private BillStatus status;
    private String paymentMethod;
    private String notes;
    private LocalDateTime createdAt;
}
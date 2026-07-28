package com.hospital.hms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillRequestDTO {

    @NotNull(message = "Appointment ID is required")
    private Long appointmentId;

    private Double additionalCharges = 0.0;

    private String notes;
}
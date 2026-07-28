package com.hospital.hms.dto.request;

import com.hospital.hms.model.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AppointmentStatusUpdateDTO {

    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    private String notes;
}
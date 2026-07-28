package com.hospital.hms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BillPaymentDTO {

    @NotBlank(message = "Payment method is required")
    private String paymentMethod; // CASH, CARD, UPI
}
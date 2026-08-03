package com.hospital.hms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PaymentOrderResponseDTO {
    private String orderId;
    private Long amountInPaise;
    private String currency;
    private String keyId;
    private Long billId;
}
package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPaymentOrderDTO {
    private String orderId;
    private long amountInPaise;
    private String currency;
    private String keyId;
}
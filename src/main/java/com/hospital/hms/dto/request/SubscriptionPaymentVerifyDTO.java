package com.hospital.hms.dto.request;
 
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
 
@Data
public class SubscriptionPaymentVerifyDTO {
    @NotBlank private String plan; // "BASIC" or "PREMIUM"
    @NotBlank private String razorpayOrderId;
    @NotBlank private String razorpayPaymentId;
    @NotBlank private String razorpaySignature;
}
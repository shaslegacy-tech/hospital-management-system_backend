package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionResponseDTO {
    private String plan;
    private String expiresAt;
    private boolean active;
    private double monthlyPrice;
}
package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PlatformStatsResponseDTO {
    private long totalHospitals;
    private long approvedHospitals;
    private long pendingHospitals;
    private long suspendedHospitals;
    private long trialHospitals;
    private long basicSubscribers;
    private long premiumSubscribers;
    private double monthlyRecurringRevenue; // MRR — sum of active paid plans
    private double totalRevenueCollected;   // all-time, from payment history
    private long totalDoctorsOnPlatform;
    private long totalPatientsOnPlatform;
}
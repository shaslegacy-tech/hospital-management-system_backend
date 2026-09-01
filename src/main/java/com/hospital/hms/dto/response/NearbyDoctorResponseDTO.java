package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyDoctorResponseDTO {
    private Long id;
    private String doctorName;
    private String specialization;
    private String departmentName;
    private double consultationFee;
    private Double averageRating;
    private Integer reviewCount;
    private Long hospitalId;
    private String hospitalName;
    private String hospitalCity;
    private double distanceKm;
}   
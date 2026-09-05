package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class NearbyHospitalResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String description;
    private String logoUrl;
    private double distanceKm;
    private boolean verified;
}
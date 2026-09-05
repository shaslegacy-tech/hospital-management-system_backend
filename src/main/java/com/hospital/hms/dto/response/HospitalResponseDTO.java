package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class HospitalResponseDTO {
    private Long id;
    private String name;
    private String address;
    private String city;
    private String state;
    private String pincode;
    private Double latitude;
    private Double longitude;
    private String contactPhone;
    private String contactEmail;
    private String description;
    private String logoUrl;
    private String status;
    private boolean verified;
}
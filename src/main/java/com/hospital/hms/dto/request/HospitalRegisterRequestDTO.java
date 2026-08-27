package com.hospital.hms.dto.request;
 
import jakarta.validation.constraints.*;
import lombok.Data;
 
@Data
public class HospitalRegisterRequestDTO {
    // Hospital details
    @NotBlank private String hospitalName;
    @NotBlank private String address;
    @NotBlank private String city;
    @NotBlank private String state;
    @NotBlank private String pincode;
    private String description;
    @NotBlank private String contactPhone;
 
    @NotBlank private String adminName;
    @NotBlank @Email private String adminEmail;
    @NotBlank @Size(min = 6) private String adminPassword;
    @NotBlank private String adminPhone;
}
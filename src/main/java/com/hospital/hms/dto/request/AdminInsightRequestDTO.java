package com.hospital.hms.dto.request;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
@Data
public class AdminInsightRequestDTO {
    @NotBlank
    @Size(max = 500)
    private String question;
}
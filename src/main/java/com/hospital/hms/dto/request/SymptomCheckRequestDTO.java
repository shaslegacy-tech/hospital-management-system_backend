package com.hospital.hms.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class SymptomCheckRequestDTO {
    @NotBlank
    @Size(max = 1000, message = "Please keep your description under 1000 characters")
    private String symptoms;
}
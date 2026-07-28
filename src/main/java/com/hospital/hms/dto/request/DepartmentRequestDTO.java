package com.hospital.hms.dto.request;

import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentRequestDTO {

    @NotBlank(message = "Department name is required")
    private String name;

    private String description;
}
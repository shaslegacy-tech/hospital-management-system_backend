package com.hospital.hms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SymptomCheckResponseDTO {
    private Long departmentId;
    private String departmentName;
    private String explanation;
    private String urgencyLevel; // LOW, MEDIUM, HIGH
    private String disclaimer;
}
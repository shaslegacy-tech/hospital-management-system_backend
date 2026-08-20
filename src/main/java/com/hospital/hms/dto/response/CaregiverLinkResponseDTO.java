package com.hospital.hms.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverLinkResponseDTO {

    private Long id;

    private Long patientId;

    private String patientName;

    private String caregiverName;

    private String caregiverEmail;

    private String relationship;
}
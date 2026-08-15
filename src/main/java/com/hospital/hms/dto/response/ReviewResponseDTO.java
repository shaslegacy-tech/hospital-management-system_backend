package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ReviewResponseDTO {
    private Long id;
    private Long appointmentId;
    private String patientName;
    private Long doctorId;
    private Integer rating;
    private String comment;
    private String createdAt;
}
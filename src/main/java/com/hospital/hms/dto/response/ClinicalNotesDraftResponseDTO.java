package com.hospital.hms.dto.response;
 
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
 
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClinicalNotesDraftResponseDTO {
    private String diagnosis;
    private String treatment;
    private String notice; // shown to the doctor as a reminder to review
}
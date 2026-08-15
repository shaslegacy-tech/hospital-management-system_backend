package com.hospital.hms.dto.request;
 
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
 
@Data
public class ClinicalNotesDraftRequestDTO {
    @NotBlank
    @Size(max = 1000)
    private String quickNotes;
}
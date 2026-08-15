package com.hospital.hms.controller;

import com.hospital.hms.dto.request.ClinicalNotesDraftRequestDTO;
import com.hospital.hms.dto.request.SymptomCheckRequestDTO;
import com.hospital.hms.dto.response.ClinicalNotesDraftResponseDTO;
import com.hospital.hms.dto.response.SymptomCheckResponseDTO;
import com.hospital.hms.service.ai.AiClinicalNotesService;
import com.hospital.hms.service.ai.AiSymptomCheckerService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "AI", description = "AI-assisted features")
@Slf4j
@RestController
@RequestMapping("/api/ai")
public class AiController {

    @Autowired
    private AiSymptomCheckerService symptomCheckerService;

    @Autowired
    private AiClinicalNotesService aiClinicalNotesService;

    @Operation(
            summary = "Suggest a department based on described symptoms",
            description = "Uses Claude to route the patient to the right " +
                    "department. Never diagnoses — routing suggestion only."
    )
    @PostMapping("/symptom-check")
    @PreAuthorize("hasAnyRole('PATIENT','RECEPTIONIST','ADMIN')")
    public ResponseEntity<SymptomCheckResponseDTO> symptomCheck(
            @Valid @RequestBody SymptomCheckRequestDTO dto) {
        log.info("Symptom check requested");
        return ResponseEntity.ok(symptomCheckerService.checkSymptoms(dto.getSymptoms()));
    }

    @Operation(
        summary = "Draft a diagnosis/treatment from a doctor's quick notes",
        description = "Doctor-only. Turns informal shorthand notes into a " +
                    "structured draft the doctor reviews and edits before " +
                    "saving as an actual medical record."
    )
    @PostMapping("/clinical-notes-draft")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ClinicalNotesDraftResponseDTO> draftClinicalNotes(
            @Valid @RequestBody ClinicalNotesDraftRequestDTO dto) {
        log.info("Clinical notes draft requested");
        return ResponseEntity.ok(aiClinicalNotesService.draft(dto.getQuickNotes()));
    }
}
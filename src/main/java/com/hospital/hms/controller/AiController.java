package com.hospital.hms.controller;

import com.hospital.hms.dto.request.SymptomCheckRequestDTO;
import com.hospital.hms.dto.response.SymptomCheckResponseDTO;
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
}
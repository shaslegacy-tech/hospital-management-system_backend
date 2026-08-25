package com.hospital.hms.controller;

import com.hospital.hms.dto.request.MedicalRecordRequestDTO;
import com.hospital.hms.dto.response.MedicalRecordResponseDTO;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Doctor;
import com.hospital.hms.model.MedicalRecord;
import com.hospital.hms.model.Patient;
import com.hospital.hms.model.User;

import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.MedicalRecordRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.UserRepository;

import com.hospital.hms.service.CaregiverAccessService;
import com.hospital.hms.service.MedicalRecordService;
import com.hospital.hms.service.ai.AiVisitSummaryService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.security.access.AccessDeniedException;

import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(
        name = "Medical Records",
        description = "Diagnosis and treatment records"
)
@Slf4j
@RestController
@RequestMapping("/api/records")
public class MedicalRecordController {

    @Autowired
    private MedicalRecordService medicalRecordService;

    @Autowired
    private AiVisitSummaryService aiVisitSummaryService;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private CaregiverAccessService caregiverAccessService;

    @Autowired
    private DoctorRepository doctorRepository;


    // ============================================================
    // Helper methods
    // ============================================================

    private User currentUser(Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found!"
                        )
                );
    }


    private Patient getPatient(Long patientId) {

        return patientRepository
                .findById(patientId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found!"
                        )
                );
    }


    private void validatePatientAccess(
        Authentication authentication,
        Patient patient) {

    User user = currentUser(authentication);

    String role = user.getRole().name();

    // ADMIN can access all patient records
    if ("ADMIN".equals(role)) {
        return;
    }

    // PATIENT can access only their own records
    if ("PATIENT".equals(role)) {
        if (!user.getId().equals(patient.getUser().getId())) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this patient's data."
            );
        }
        return;
    }

    // DOCTOR can access patients who have appointments with them
    if ("DOCTOR".equals(role)) {

        Doctor doctor = doctorRepository
                .findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException("Doctor profile not found!")
                );

        boolean hasAppointment =
                appointmentRepository
                        .existsByDoctorIdAndPatientId(
                                doctor.getId(),
                                patient.getId()
                        );

        if (!hasAppointment) {
            throw new org.springframework.security.access.AccessDeniedException(
                    "You do not have access to this patient's data."
            );
        }

        return;
    }

//     // CAREGIVER access
//     if ("CAREGIVER".equals(role)) {

//         caregiverAccessService.validateAccess(
//                 user.getId(),
//                 patient.getUser().getId(),
//                 patient.getId()
//         );

//         return;
//     }

    throw new org.springframework.security.access.AccessDeniedException(
            "You do not have access to this patient's data."
    );
}


    // ============================================================
    // GET record by ID
    // PATIENT = own
    // CAREGIVER = managed patient
    // DOCTOR / ADMIN = existing access
    // ============================================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole('PATIENT','DOCTOR','ADMIN')"
    )
    public ResponseEntity<MedicalRecordResponseDTO> getById(
            @PathVariable Long id,
            Authentication authentication) {

        MedicalRecord record =
                medicalRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Medical record not found!"
                                )
                        );

        User user = currentUser(authentication);

        String role = user.getRole().name();


            validatePatientAccess(
                    authentication,
                    record.getAppointment().getPatient()
            );

        return ResponseEntity.ok(
                medicalRecordService.getById(id)
        );
    }


    // ============================================================
    // GET record by appointment
    // ============================================================

    @GetMapping("/appointment/{appointmentId}")
    @PreAuthorize(
            "hasAnyRole('PATIENT','DOCTOR','ADMIN')"
    )
    public ResponseEntity<MedicalRecordResponseDTO>
    getByAppointment(
            @PathVariable Long appointmentId,
            Authentication authentication) {

        Appointment appointment =
                appointmentRepository
                        .findById(appointmentId)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found!"
                                )
                        );

        User user = currentUser(authentication);

        String role = user.getRole().name();
            validatePatientAccess(
                    authentication,
                    appointment.getPatient()
            );
        return ResponseEntity.ok(
                medicalRecordService
                        .getByAppointmentId(appointmentId)
        );
    }



    @GetMapping("/patient/{patientId}/history")
    @PreAuthorize(
            "hasAnyRole('PATIENT','DOCTOR','ADMIN')"
    )
    public ResponseEntity<List<MedicalRecordResponseDTO>>
    getPatientHistory(
            @PathVariable Long patientId,
            Authentication authentication) {

        Patient patient = getPatient(patientId);

        validatePatientAccess(
                authentication,
                patient
        );

        return ResponseEntity.ok(
                medicalRecordService
                        .getPatientHistory(patientId)
        );
    }


    @PostMapping
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponseDTO>
    createRecord(
            @Valid @RequestBody
            MedicalRecordRequestDTO dto) {

        return ResponseEntity.ok(
                medicalRecordService.createRecord(dto)
        );
    }



    @PutMapping("/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<MedicalRecordResponseDTO>
    updateRecord(
            @PathVariable Long id,
            @Valid @RequestBody
            MedicalRecordRequestDTO dto) {

        return ResponseEntity.ok(
                medicalRecordService.updateRecord(
                        id,
                        dto
                )
        );
    }


    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<MedicalRecordResponseDTO>>
    getAllRecords() {

        return ResponseEntity.ok(
                medicalRecordService.getAllRecords()
        );
    }


    @Operation(
            summary = "Get a plain-language explanation "
                    + "of a medical record",
            description =
                    "Generates and caches an AI explanation "
                    + "of the diagnosis and treatment. "
                    + "Only the patient or an authorized "
                    + "caregiver can request it."
    )
    @PostMapping("/{id}/explain")
    @PreAuthorize(
            "hasAnyRole('PATIENT','DOCTOR','ADMIN')"
    )
    public ResponseEntity<String> explainRecord(
            @PathVariable Long id,
            Authentication authentication) {

        log.info(
                "Explain-record requested for record {}",
                id
        );

        MedicalRecord record =
                medicalRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Record not found!"
                                )
                        );

        Patient patient =
                record.getAppointment().getPatient();

        validatePatientAccess(
                authentication,
                patient
        );


        // Generate AI summary only once
        if (record.getPatientSummary() == null
                || record.getPatientSummary().isBlank()) {

            String summary =
                    aiVisitSummaryService.summarize(record);

            record.setPatientSummary(summary);

            medicalRecordRepository.save(record);
        }

        return ResponseEntity.ok(
                record.getPatientSummary()
        );
    }
}
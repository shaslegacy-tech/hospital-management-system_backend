package com.hospital.hms.controller;

import com.hospital.hms.dto.request.AppointmentRequestDTO;
import com.hospital.hms.dto.request.AppointmentStatusUpdateDTO;
import com.hospital.hms.dto.response.AppointmentResponseDTO;
import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Patient;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.AppointmentService;
import com.hospital.hms.service.CaregiverAccessService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(
        name = "Appointments",
        description = "Book and manage appointments"
)
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CaregiverAccessService caregiverAccessService;


    // ============================================================
    // Helper methods
    // ============================================================

    private User currentUser(Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException("User not found!")
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
            Long patientId) {

        User user = currentUser(authentication);

        Patient patient = getPatient(patientId);

        caregiverAccessService.validateAccess(
                user.getId(),
                patient.getUser().getId(),
                patient.getId()
        );
    }


    // ============================================================
    // GET all appointments
    // ADMIN, RECEPTIONIST
    // ============================================================

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','RECEPTIONIST')"
    )
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                appointmentService.getAllAppointments(
                        page,
                        size
                )
        );
    }


    // ============================================================
    // GET appointment by ID
    // ============================================================

        @GetMapping("/{id}")
        @PreAuthorize(
                "hasAnyRole('ADMIN','PATIENT','RECEPTIONIST','DOCTOR')"
        )
        public ResponseEntity<AppointmentResponseDTO> getAppointmentById(
                @PathVariable Long id,
                Authentication authentication) {

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found!"
                                )
                        );

        User user = currentUser(authentication);

        String role = user.getRole().name();

        if (role.equals("PATIENT") || role.equals("CAREGIVER")) {
                validatePatientAccess(
                        authentication,
                        appointment.getPatient().getId()
                );
        }

        return ResponseEntity.ok(
                appointmentService.getAppointmentById(id)
        );
        }
    // ============================================================
    // GET today's appointments
    // ADMIN, RECEPTIONIST
    // ============================================================

    @GetMapping("/today")
    @PreAuthorize(
            "hasAnyRole('ADMIN','RECEPTIONIST')"
    )
    public ResponseEntity<List<AppointmentResponseDTO>>
    getTodaysAppointments() {

        return ResponseEntity.ok(
                appointmentService.getTodaysAppointments()
        );
    }


    // ============================================================
    // GET appointments by patient
    // PATIENT = own
    // CAREGIVER = linked patient
    // ============================================================

    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getByPatient(
            @PathVariable Long patientId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            Authentication authentication) {

        validatePatientAccess(
                authentication,
                patientId
        );

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatient(
                        patientId,
                        page,
                        size
                )
        );
    }


    // ============================================================
    // GET by doctor
    // ============================================================

    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize(
            "hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getByDoctor(
            @PathVariable Long doctorId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctor(
                        doctorId,
                        page,
                        size
                )
        );
    }


    // ============================================================
    // GET doctor schedule
    // ============================================================

    @GetMapping("/doctor/{doctorId}/schedule")
    @PreAuthorize(
            "hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<List<AppointmentResponseDTO>>
    getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate date) {

        return ResponseEntity.ok(
                appointmentService.getDoctorSchedule(
                        doctorId,
                        date
                )
        );
    }


    // ============================================================
    // BOOK appointment
    // PATIENT = own
    // CAREGIVER = linked patient
    // ============================================================

    @Operation(
            summary = "Book a new appointment",
            description =
                    "Patient or authorized caregiver can book "
                    + "an appointment. System checks doctor "
                    + "availability and slot conflicts."
    )
        @PostMapping
        @PreAuthorize(
                "hasAnyRole('ADMIN','PATIENT','RECEPTIONIST')"
        )
        public ResponseEntity<AppointmentResponseDTO> bookAppointment(
                @Valid @RequestBody AppointmentRequestDTO dto,
                Authentication authentication) {

        User user = currentUser(authentication);

        String role = user.getRole().name();

        // Patient/Caregiver must have access to the target patient
        if (role.equals("PATIENT")) {
                validatePatientAccess(
                        authentication,
                        dto.getPatientId()
                );
        }

        return ResponseEntity.ok(
                appointmentService.bookAppointment(dto)
        );
        }


    // ============================================================
    // UPDATE STATUS
    // ADMIN, DOCTOR, RECEPTIONIST
    // ============================================================

    @PutMapping("/{id}/status")
    @PreAuthorize(
            "hasAnyRole('ADMIN','DOCTOR','RECEPTIONIST')"
    )
    public ResponseEntity<AppointmentResponseDTO>
    updateStatus(
            @PathVariable Long id,
            @Valid @RequestBody
            AppointmentStatusUpdateDTO dto) {

        return ResponseEntity.ok(
                appointmentService.updateStatus(
                        id,
                        dto
                )
        );
    }


    // ============================================================
    // CANCEL appointment
    // ============================================================

        @PutMapping("/{id}/cancel")
        @PreAuthorize(
                "hasAnyRole('ADMIN','PATIENT','RECEPTIONIST')"
        )
        public ResponseEntity<AppointmentResponseDTO> cancelAppointment(
                @PathVariable Long id,
                Authentication authentication) {

        Appointment appointment =
                appointmentRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found!"
                                )
                        );

        User user = currentUser(authentication);

        String role = user.getRole().name();

        // Patient/Caregiver must have access
        // Admin/Receptionist can perform operational cancellation
        if (role.equals("PATIENT") || role.equals("CAREGIVER")) {
                validatePatientAccess(
                        authentication,
                        appointment.getPatient().getId()
                );
        }

        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id)
        );
        }


    // ============================================================
    // DELETE appointment
    // ADMIN only
    // ============================================================

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String>
    deleteAppointment(
            @PathVariable Long id) {

        appointmentService.deleteAppointment(id);

        return ResponseEntity.ok(
                "Appointment deleted successfully"
        );
    }


    // ============================================================
    // SEARCH appointments
    // ADMIN, RECEPTIONIST
    // ============================================================

    @Operation(
            summary = "Search appointments with filters",
            description =
                    "Filter by status, doctor, patient, "
                    + "department, and date range"
    )
    @GetMapping("/search")
    @PreAuthorize(
            "hasAnyRole('ADMIN','RECEPTIONIST')"
    )
    public ResponseEntity<Page<AppointmentResponseDTO>>
    searchAppointments(
            @RequestParam(required = false)
            AppointmentStatus status,

            @RequestParam(required = false)
            Long doctorId,

            @RequestParam(required = false)
            Long patientId,

            @RequestParam(required = false)
            Long departmentId,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateFrom,

            @RequestParam(required = false)
            @DateTimeFormat(
                    iso = DateTimeFormat.ISO.DATE
            )
            LocalDate dateTo,

            @RequestParam(defaultValue = "0")
            int page,

            @RequestParam(defaultValue = "10")
            int size) {

        return ResponseEntity.ok(
                appointmentService.searchAppointments(
                        status,
                        doctorId,
                        patientId,
                        departmentId,
                        dateFrom,
                        dateTo,
                        page,
                        size
                )
        );
    }
}
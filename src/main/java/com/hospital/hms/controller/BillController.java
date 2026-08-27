package com.hospital.hms.controller;

import com.hospital.hms.dto.request.BillPaymentDTO;
import com.hospital.hms.dto.request.BillRequestDTO;
import com.hospital.hms.dto.response.BillResponseDTO;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Bill;
import com.hospital.hms.model.Patient;
import com.hospital.hms.model.User;

import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.BillRepository;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.UserRepository;

import com.hospital.hms.service.BillService;
import com.hospital.hms.service.CaregiverAccessService;

import io.swagger.v3.oas.annotations.tags.Tag;

import jakarta.validation.Valid;

import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.data.domain.Page;

import org.springframework.http.ResponseEntity;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;

import org.springframework.web.bind.annotation.*;

import java.util.List;


@Tag(
        name = "Billing",
        description = "Bill generation and payments"
)
@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    @Autowired
    private BillRepository billRepository;

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

    private User currentUser(
            Authentication authentication) {

        return userRepository
                .findByEmail(authentication.getName())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found!"
                        )
                );
    }


    private Patient getPatient(
            Long patientId) {

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

        User user =
                currentUser(authentication);

        caregiverAccessService.validateAccess(
                user.getId(),
                patient.getUser().getId(),
                patient.getId()
        );
    }


    // ============================================================
    // GET bill by ID
    //
    // PATIENT    → own bill
    // CAREGIVER  → managed patient's bill
    // ADMIN      → all
    // RECEPTIONIST → operational access
    // ============================================================

    @GetMapping("/{id}")
    @PreAuthorize(
            "hasAnyRole("
                    + "'PATIENT',"
                    + "'ADMIN',"
                    + "'RECEPTIONIST'"
                    + ")"
    )
    public ResponseEntity<BillResponseDTO>
    getBillById(
            @PathVariable Long id,
            Authentication authentication) {

        Bill bill =
                billRepository
                        .findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Bill not found!"
                                )
                        );

        User user =
                currentUser(authentication);

        String role =
                user.getRole().name();


        if (role.equals("PATIENT")
                || role.equals("CAREGIVER")) {

            validatePatientAccess(
                    authentication,
                    bill.getAppointment()
                            .getPatient()
            );
        }


        return ResponseEntity.ok(
                billService.getBillById(id, authentication)
        );
    }


    // ============================================================
    // GET bill by appointment
    // ============================================================

    @GetMapping(
            "/appointment/{appointmentId}"
    )
    @PreAuthorize(
            "hasAnyRole("
                    + "'PATIENT',"
                    + "'ADMIN',"
                    + "'RECEPTIONIST'"
                    + ")"
    )
    public ResponseEntity<BillResponseDTO>
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


        User user =
                currentUser(authentication);

        String role =
                user.getRole().name();


        if (role.equals("PATIENT")
                || role.equals("CAREGIVER")) {

            validatePatientAccess(
                    authentication,
                    appointment.getPatient()
            );
        }


        return ResponseEntity.ok(
                billService.getBillByAppointment(
                        appointmentId,
                        authentication
                )
        );
    }


    // ============================================================
    // GET bills by patient
    //
    // PATIENT    → own bills
    // CAREGIVER  → managed patient's bills
    // ============================================================

    @GetMapping(
            "/patient/{patientId}"
    )
//     @PreAuthorize(
//             "hasAnyRole('PATIENT','CAREGIVER')"
//     )
    public ResponseEntity<List<BillResponseDTO>>
    getByPatient(
            @PathVariable Long patientId,
            Authentication authentication) {

        Patient patient =
                getPatient(patientId);


        validatePatientAccess(
                authentication,
                patient
        );


        return ResponseEntity.ok(
                billService.getBillsByPatient(
                        patientId,
                        authentication
                )
        );
    }


    // ============================================================
    // POST - generate bill
    // ADMIN, RECEPTIONIST
    // ============================================================

    @PostMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','RECEPTIONIST')"
    )
    public ResponseEntity<BillResponseDTO>
    generateBill(
                        @Valid @RequestBody
                        BillRequestDTO dto,
                        Authentication authentication) {

        return ResponseEntity.ok(
                                billService.generateBill(dto, authentication)
        );
    }


    // ============================================================
    // PUT - pay bill
    // ADMIN, RECEPTIONIST
    // ============================================================

    @PutMapping("/{id}/pay")
    @PreAuthorize(
            "hasAnyRole('ADMIN','RECEPTIONIST')"
    )
    public ResponseEntity<BillResponseDTO>
    payBill(
            @PathVariable Long id,
            @Valid @RequestBody
            BillPaymentDTO dto,
            Authentication authentication) {

        return ResponseEntity.ok(
                billService.payBill(
                        id,
                        dto,
                        authentication
                )
        );
    }


    // ============================================================
    // GET all bills
    // ADMIN, RECEPTIONIST
    // ============================================================

    @GetMapping
    @PreAuthorize(
            "hasAnyRole('ADMIN','RECEPTIONIST')"
    )
    public ResponseEntity<Page<BillResponseDTO>>
    getAllBills(
            @RequestParam(
                    defaultValue = "0"
            )
            int page,

            @RequestParam(
                    defaultValue = "20"
            )
            int size,
            Authentication authentication) {

        return ResponseEntity.ok(
                billService.getAllBills(
                        authentication,
                        page,
                        size
                )
        );
    }
}
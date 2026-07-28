package com.hospital.hms.controller;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AppointmentResponseDTO;
import com.hospital.hms.dto.response.BillResponseDTO;
import com.hospital.hms.service.BillService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Billing",
        description = "Bill generation and payments")
@RestController
@RequestMapping("/api/bills")
public class BillController {

    @Autowired
    private BillService billService;

    // GET bill by ID
    @GetMapping("/{id}")
    public ResponseEntity<BillResponseDTO>
    getBillById(@PathVariable Long id) {
        return ResponseEntity.ok(
                billService.getBillById(id));
    }

    // GET bill by appointment
    @GetMapping("/appointment/{appointmentId}")
    public ResponseEntity<BillResponseDTO>
    getByAppointment(
            @PathVariable Long appointmentId) {
        return ResponseEntity.ok(
                billService.getBillByAppointment(appointmentId));
    }

    // GET bills by patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<List<BillResponseDTO>>
    getByPatient(@PathVariable Long patientId) {
        return ResponseEntity.ok(
                billService.getBillsByPatient(patientId));
    }

    // POST - generate bill — ADMIN, RECEPTIONIST
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<BillResponseDTO> generateBill(
            @Valid @RequestBody BillRequestDTO dto) {
        return ResponseEntity.ok(
                billService.generateBill(dto));
    }

    // PUT - pay bill — ADMIN, RECEPTIONIST
    @PutMapping("/{id}/pay")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<BillResponseDTO> payBill(
            @PathVariable Long id,
            @Valid @RequestBody BillPaymentDTO dto) {
        return ResponseEntity.ok(
                billService.payBill(id, dto));
    }

    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<Page<BillResponseDTO>> getAllBills(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        return ResponseEntity.ok(
                billService.getAllBills(
                        page, size));
    }
}
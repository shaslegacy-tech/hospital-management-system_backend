package com.hospital.hms.controller;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AppointmentResponseDTO;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@Tag(name = "Appointments",
        description = "Book and manage appointments")
@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    // GET all — ADMIN, RECEPTIONIST
    @GetMapping
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getAllAppointments(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                appointmentService.getAllAppointments(
                        page, size));
    }

    // GET by ID
    @GetMapping("/{id}")
    public ResponseEntity<AppointmentResponseDTO>
    getAppointmentById(@PathVariable Long id) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentById(id));
    }

    // GET today's appointments — ADMIN, RECEPTIONIST
    @GetMapping("/today")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getTodaysAppointments() {
        return ResponseEntity.ok(
                appointmentService.getTodaysAppointments());
    }

    // GET by patient
    @GetMapping("/patient/{patientId}")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getByPatient(@PathVariable Long patientId,
                 @RequestParam(defaultValue = "0") int page,
                 @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByPatient(
                        patientId, page, size));
    }

    // GET by doctor
    @GetMapping("/doctor/{doctorId}")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    getByDoctor(@PathVariable Long doctorId,
                @RequestParam(defaultValue = "0") int page,
                @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(
                appointmentService.getAppointmentsByDoctor(
                        doctorId, page, size));
    }

    // GET doctor schedule for date
    @GetMapping("/doctor/{doctorId}/schedule")
    public ResponseEntity<List<AppointmentResponseDTO>>
    getDoctorSchedule(
            @PathVariable Long doctorId,
            @RequestParam
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate date) {
        return ResponseEntity.ok(
                appointmentService.getDoctorSchedule(
                        doctorId, date));
    }

    @Operation(
            summary = "Book a new appointment",
            description = "Patient books appointment with a " +
                    "doctor. System checks doctor " +
                    "availability and slot conflicts " +
                    "automatically. Sends confirmation " +
                    "email to patient."
    )
    // POST - book appointment
    @PostMapping
    @PreAuthorize("hasAnyRole('ADMIN','PATIENT'," +
            "'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDTO>
    bookAppointment(
            @Valid @RequestBody
            AppointmentRequestDTO dto) {
        return ResponseEntity.ok(
                appointmentService.bookAppointment(dto));
    }

    // PUT - update status — ADMIN, DOCTOR, RECEPTIONIST
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR'," +
            "'RECEPTIONIST')")
    public ResponseEntity<AppointmentResponseDTO>
    updateStatus(@PathVariable Long id,
                 @Valid @RequestBody
                 AppointmentStatusUpdateDTO dto) {
        return ResponseEntity.ok(
                appointmentService.updateStatus(id, dto));
    }

    // PUT - cancel
    @PutMapping("/{id}/cancel")
    public ResponseEntity<AppointmentResponseDTO>
    cancelAppointment(@PathVariable Long id) {
        return ResponseEntity.ok(
                appointmentService.cancelAppointment(id));
    }

    // DELETE — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteAppointment(
            @PathVariable Long id) {
        appointmentService.deleteAppointment(id);
        return ResponseEntity.ok(
                "Appointment deleted successfully");
    }

    @Operation(
            summary = "Search appointments with filters",
            description = "Filter by status, doctor, patient, " +
                    "department, and date range"
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<Page<AppointmentResponseDTO>>
    searchAppointments(
            @RequestParam(required = false)
            AppointmentStatus status,
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) Long patientId,
            @RequestParam(required = false)
            Long departmentId,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateFrom,
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate dateTo,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {

        return ResponseEntity.ok(
                appointmentService.searchAppointments(
                        status, doctorId, patientId, departmentId,
                        dateFrom, dateTo, page, size));
    }
}
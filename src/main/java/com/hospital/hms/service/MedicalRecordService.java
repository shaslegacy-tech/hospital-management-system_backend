package com.hospital.hms.service;

import com.hospital.hms.dto.request.MedicalRecordRequestDTO;
import com.hospital.hms.dto.response.*;
import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.repository.*;
import com.hospital.hms.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class MedicalRecordService {

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    private PrescriptionResponseDTO toPrescriptionDTO(
            Prescription p) {
        return new PrescriptionResponseDTO(
                p.getId(),
                p.getMedicineName(),
                p.getDosage(),
                p.getDuration(),
                p.getInstructions(),
                p.getCreatedAt()
        );
    }

    private MedicalRecordResponseDTO toDTO(
            MedicalRecord record) {
        List<PrescriptionResponseDTO> prescriptions =
                prescriptionRepository
                        .findByMedicalRecordId(record.getId())
                        .stream()
                        .map(this::toPrescriptionDTO)
                        .collect(Collectors.toList());

        return new MedicalRecordResponseDTO(
                record.getId(),
                record.getAppointment().getId(),
                record.getAppointment()
                        .getPatient().getUser().getName(),
                record.getAppointment()
                        .getDoctor().getUser().getName(),
                record.getDiagnosis(),
                record.getTreatment(),
                record.getNotes(),
                prescriptions,
                record.getCreatedAt()
        );
    }

    // GET record by appointment ID
    public MedicalRecordResponseDTO getByAppointmentId(
            Long appointmentId) {
        MedicalRecord record = medicalRecordRepository
                .findByAppointmentId(appointmentId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Medical record not found!"));
        return toDTO(record);
    }

    // GET all records for a patient
    public List<MedicalRecordResponseDTO>
    getPatientHistory(Long patientId) {
        log.info("Fetching medical history for patient: {}",
                patientId);
        return medicalRecordRepository
                .findByAppointmentPatientId(patientId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET record by ID
    public MedicalRecordResponseDTO getById(Long id) {
        MedicalRecord record =
                medicalRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Medical record not found: " + id));
        return toDTO(record);
    }

    // POST - create medical record (DOCTOR only)
    public MedicalRecordResponseDTO createRecord(
            MedicalRecordRequestDTO dto) {
        log.info("Creating medical record for appointment: {}",
                dto.getAppointmentId());

        Appointment appointment =
                appointmentRepository
                        .findById(dto.getAppointmentId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Appointment not found!"));

        // ✅ Block only CANCELLED appointments
        if (appointment.getStatus() ==
                AppointmentStatus.CANCELLED) {
            throw new RuntimeException(
                    "Cannot add record to a cancelled appointment!");
        }

        // ✅ Block only PENDING appointments
        if (appointment.getStatus() ==
                AppointmentStatus.PENDING) {
            throw new RuntimeException(
                    "Appointment must be CONFIRMED or COMPLETED first!");
        }

        // Check record doesn't already exist
        if (medicalRecordRepository
                .findByAppointmentId(
                        dto.getAppointmentId()).isPresent()) {
            throw new RuntimeException(
                    "Medical record already exists " +
                            "for this appointment!");
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());

        // Auto-complete the appointment if not already
        if (appointment.getStatus() !=
                AppointmentStatus.COMPLETED) {
            appointment.setStatus(AppointmentStatus.COMPLETED);
            appointmentRepository.save(appointment);
        }

        MedicalRecord saved =
                medicalRecordRepository.save(record);

        emailService.sendMedicalRecordAddedEmail(saved);

        // Notify patient — diagnosis added
        notificationService.notify(
                saved.getAppointment()
                        .getPatient().getUser(),
                "RECORD_ADDED",
                "Dr. " +
                        saved.getAppointment()
                                .getDoctor().getUser().getName() +
                        " added a diagnosis for your visit",
                "/records"
        );

        // Notify patient — appointment completed
        notificationService.notify(
                saved.getAppointment()
                        .getPatient().getUser(),
                "APPOINTMENT_COMPLETED",
                "Your appointment with Dr. " +
                        saved.getAppointment()
                                .getDoctor().getUser().getName() +
                        " has been marked as completed",
                "/appointments"
        );

        log.info("Medical record created, appointment " +
                "COMPLETED, notifications sent!");
        return toDTO(saved);
    }

    // PUT - update record
    public MedicalRecordResponseDTO updateRecord(
            Long id,
            MedicalRecordRequestDTO dto) {
        MedicalRecord record =
                medicalRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Medical record not found: " + id));

        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());

        MedicalRecord saved =
                medicalRecordRepository.save(record);
//        emailService.sendMedicalRecordAddedEmail(saved);
        // Notify patient — record updated
        notificationService.notify(
                saved.getAppointment().getPatient().getUser(),
                "RECORD_ADDED",
                "Dr. " +
                        saved.getAppointment()
                                .getDoctor().getUser().getName() +
                        " updated your medical record",
                "/records"
        );

        return toDTO(saved);
    }

    // GET all medical records — ADMIN only
    public List<MedicalRecordResponseDTO> getAllRecords() {
        log.info("Fetching all medical records");
        return medicalRecordRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

}

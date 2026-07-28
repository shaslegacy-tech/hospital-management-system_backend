package com.hospital.hms.service;

import com.hospital.hms.dto.request.MedicalRecordRequestDTO;
import com.hospital.hms.dto.response.*;
import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.repository.*;
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

        // Check appointment is CONFIRMED
        if (appointment.getStatus() !=
                AppointmentStatus.CONFIRMED) {
            throw new RuntimeException(
                    "Appointment must be CONFIRMED first!");
        }

        // Check record doesn't already exist
        if (medicalRecordRepository
                .findByAppointmentId(
                        dto.getAppointmentId()).isPresent()) {
            throw new RuntimeException(
                    "Medical record already exists!");
        }

        MedicalRecord record = new MedicalRecord();
        record.setAppointment(appointment);
        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());

        // Auto-complete the appointment
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Medical record created, appointment COMPLETED");
        return toDTO(medicalRecordRepository.save(record));
    }

    // PUT - update record
    public MedicalRecordResponseDTO updateRecord(Long id,
                                                 MedicalRecordRequestDTO dto) {
        MedicalRecord record =
                medicalRecordRepository.findById(id)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Medical record not found: " + id));

        record.setDiagnosis(dto.getDiagnosis());
        record.setTreatment(dto.getTreatment());
        record.setNotes(dto.getNotes());

        return toDTO(medicalRecordRepository.save(record));
    }
}
package com.hospital.hms.service;

import com.hospital.hms.dto.request.PrescriptionRequestDTO;
import com.hospital.hms.dto.response.PrescriptionResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PrescriptionService {

    @Autowired
    private PrescriptionRepository prescriptionRepository;

    @Autowired
    private MedicalRecordRepository medicalRecordRepository;

    private PrescriptionResponseDTO toDTO(Prescription p) {
        return new PrescriptionResponseDTO(
                p.getId(),
                p.getMedicineName(),
                p.getDosage(),
                p.getDuration(),
                p.getInstructions(),
                p.getCreatedAt()
        );
    }

    // GET prescriptions by medical record
    public List<PrescriptionResponseDTO>
    getByMedicalRecord(Long recordId) {
        return prescriptionRepository
                .findByMedicalRecordId(recordId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // POST - add prescription
    public PrescriptionResponseDTO addPrescription(
            PrescriptionRequestDTO dto) {
        log.info("Adding prescription for record: {}",
                dto.getMedicalRecordId());

        MedicalRecord record =
                medicalRecordRepository
                        .findById(dto.getMedicalRecordId())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "Medical record not found!"));

        Prescription prescription = new Prescription();
        prescription.setMedicalRecord(record);
        prescription.setMedicineName(dto.getMedicineName());
        prescription.setDosage(dto.getDosage());
        prescription.setDuration(dto.getDuration());
        prescription.setInstructions(dto.getInstructions());

        return toDTO(prescriptionRepository
                .save(prescription));
    }

    // DELETE prescription
    public void deletePrescription(Long id) {
        log.warn("Deleting prescription: {}", id);
        prescriptionRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Prescription not found: " + id));
        prescriptionRepository.deleteById(id);
    }
}
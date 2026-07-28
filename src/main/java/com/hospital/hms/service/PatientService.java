package com.hospital.hms.service;

import com.hospital.hms.dto.request.PatientRequestDTO;
import com.hospital.hms.dto.response.PatientResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.BloodGroup;
import com.hospital.hms.repository.*;
import com.hospital.hms.repository.spec.PatientSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private UserRepository userRepository;

    private PatientResponseDTO toDTO(Patient patient) {
        return new PatientResponseDTO(
                patient.getId(),
                patient.getUser().getId(),
                patient.getUser().getName(),
                patient.getUser().getEmail(),
                patient.getUser().getPhone(),
                patient.getDateOfBirth(),
                patient.getBloodGroup(),
                patient.getAddress(),
                patient.getEmergencyContact(),
                patient.getEmergencyContactName(),
                patient.getMedicalHistory(),
                patient.getCreatedAt()
        );
    }

    // GET all patients with pagination
    public Page<PatientResponseDTO> getAllPatients(
            int page, int size) {
        log.info("Fetching all patients page: {}", page);
        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());
        return patientRepository.findAll(pageable)
                .map(this::toDTO);
    }

    // GET patient by ID
    public PatientResponseDTO getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found: " + id));
        return toDTO(patient);
    }

    // GET patient by user ID
    public PatientResponseDTO getPatientByUserId(Long userId) {
        Patient patient = patientRepository
                .findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found for user: " +
                                        userId));
        return toDTO(patient);
    }

    // POST - create patient profile
    public PatientResponseDTO createPatient(
            PatientRequestDTO dto) {
        log.info("Creating patient for user: {}",
                dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + dto.getUserId()));

        if (!user.getRole().name().equals("PATIENT")) {
            throw new RuntimeException(
                    "User must have PATIENT role!");
        }

        if (patientRepository.findByUserId(
                dto.getUserId()).isPresent()) {
            throw new RuntimeException(
                    "Patient profile already exists!");
        }

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setEmergencyContactName(
                dto.getEmergencyContactName());
        patient.setMedicalHistory(dto.getMedicalHistory());

        return toDTO(patientRepository.save(patient));
    }

    // PUT - update patient
    public PatientResponseDTO updatePatient(Long id,
                                            PatientRequestDTO dto) {
        log.info("Updating patient: {}", id);

        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found: " + id));

        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setEmergencyContactName(
                dto.getEmergencyContactName());
        patient.setMedicalHistory(dto.getMedicalHistory());

        return toDTO(patientRepository.save(patient));
    }

    // DELETE patient
    public void deletePatient(Long id) {
        log.warn("Deleting patient: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient not found: " + id));
        patientRepository.delete(patient);
    }

    public Page<PatientResponseDTO> searchPatients(
            String name,
            String email,
            BloodGroup bloodGroup,
            int page, int size) {

        log.info("Searching patients with filters");

        Specification<Patient> spec = Specification
                .where(PatientSpecification.hasName(name))
                .and(PatientSpecification.hasEmail(email))
                .and(PatientSpecification.hasBloodGroup(
                        bloodGroup));

        Pageable pageable = PageRequest.of(page, size,
                Sort.by("createdAt").descending());

        return patientRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    // GET - Patient views their own profile
    public PatientResponseDTO getOwnProfile(String email) {
        log.info("Patient fetching their own profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient profile not found. " +
                                        "Create it first at POST /api/patients/me"));

        return toDTO(patient);
    }

    // POST - Patient creates their own profile
    public PatientResponseDTO createOwnProfile(PatientRequestDTO dto,
                                               String email) {
        log.info("Patient creating their own profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        if (patientRepository.findByUserId(user.getId()).isPresent()) {
            throw new RuntimeException(
                    "Patient profile already exists for this user!");
        }

        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setEmergencyContactName(dto.getEmergencyContactName());
        patient.setMedicalHistory(dto.getMedicalHistory());

        log.info("Patient profile created for user {}", user.getId());
        return toDTO(patientRepository.save(patient));
    }

    // PUT - Patient updates their own profile
    public PatientResponseDTO updateOwnProfile(PatientRequestDTO dto,
                                               String email) {
        log.info("Patient updating their own profile for email: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        Patient patient = patientRepository.findByUserId(user.getId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Patient profile not found. " +
                                        "Create it first at POST /api/patients/me"));

        patient.setDateOfBirth(dto.getDateOfBirth());
        patient.setBloodGroup(dto.getBloodGroup());
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContact(dto.getEmergencyContact());
        patient.setEmergencyContactName(dto.getEmergencyContactName());
        patient.setMedicalHistory(dto.getMedicalHistory());

        log.info("Patient profile updated for user {}", user.getId());
        return toDTO(patientRepository.save(patient));
    }

}
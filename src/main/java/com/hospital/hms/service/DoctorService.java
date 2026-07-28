package com.hospital.hms.service;

import com.hospital.hms.dto.request.DoctorRequestDTO;
import com.hospital.hms.dto.response.DoctorResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.repository.*;
import com.hospital.hms.repository.spec.DoctorSpecification;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    private DoctorResponseDTO toDTO(Doctor doctor) {
        return new DoctorResponseDTO(
                doctor.getId(),
                doctor.getUser().getId(),
                doctor.getUser().getName(),
                doctor.getUser().getEmail(),
                doctor.getUser().getPhone(),
                doctor.getDepartment().getName(),
                doctor.getSpecialization(),
                doctor.getExperienceYears(),
                doctor.getConsultationFee(),
                doctor.getBio(),
                doctor.isAvailable(),
                doctor.getCreatedAt()
        );
    }

    // GET all doctors
    public List<DoctorResponseDTO> getAllDoctors() {
        log.info("Fetching all doctors");
        return doctorRepository.findAll()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET doctor by ID
    public DoctorResponseDTO getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found: " + id));
        return toDTO(doctor);
    }

    // GET doctors by department
    public List<DoctorResponseDTO> getDoctorsByDepartment(
            Long departmentId) {
        log.info("Fetching doctors for department: {}",
                departmentId);
        return doctorRepository
                .findByDepartmentId(departmentId)
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET available doctors
    public List<DoctorResponseDTO> getAvailableDoctors() {
        return doctorRepository.findByAvailableTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // POST - create doctor profile
    public DoctorResponseDTO createDoctor(
            DoctorRequestDTO dto) {
        log.info("Creating doctor profile for user: {}",
                dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + dto.getUserId()));

        // Check user has DOCTOR role
        if (!user.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException(
                    "User must have DOCTOR role!");
        }

        // Check doctor profile doesn't exist
        if (doctorRepository.findByUserId(
                dto.getUserId()).isPresent()) {
            throw new RuntimeException(
                    "Doctor profile already exists!");
        }

        Department department = departmentRepository
                .findById(dto.getDepartmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found: " +
                                        dto.getDepartmentId()));

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setBio(dto.getBio());
        doctor.setAvailable(true);

        return toDTO(doctorRepository.save(doctor));
    }

    // PUT - update doctor
    public DoctorResponseDTO updateDoctor(Long id,
                                          DoctorRequestDTO dto) {
        log.info("Updating doctor: {}", id);

        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found: " + id));

        Department department = departmentRepository
                .findById(dto.getDepartmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found!"));

        doctor.setDepartment(department);
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setBio(dto.getBio());

        return toDTO(doctorRepository.save(doctor));
    }

    // PUT - toggle availability
    public DoctorResponseDTO toggleAvailability(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found: " + id));

        doctor.setAvailable(!doctor.isAvailable());
        log.info("Doctor {} availability: {}",
                id, doctor.isAvailable());

        return toDTO(doctorRepository.save(doctor));
    }

    // DELETE - remove doctor profile
    public void deleteDoctor(Long id) {
        log.warn("Deleting doctor: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found: " + id));
        doctorRepository.delete(doctor);
    }

    //Search Functionality
    public Page<DoctorResponseDTO> searchDoctors(
            String name,
            String specialization,
            Long departmentId,
            Integer minExperience,
            Boolean available,
            Double maxFee,
            int page, int size) {

        log.info("Searching doctors with filters");

        Specification<Doctor> spec = Specification
                .where(DoctorSpecification.hasName(name))
                .and(DoctorSpecification.hasSpecialization(
                        specialization))
                .and(DoctorSpecification.hasDepartment(
                        departmentId))
                .and(DoctorSpecification.minExperience(
                        minExperience))
                .and(DoctorSpecification.isAvailable(available))
                .and(DoctorSpecification.maxFee(maxFee));

        Pageable pageable = PageRequest.of(page, size);

        return doctorRepository.findAll(spec, pageable)
                .map(this::toDTO);
    }

    // GET doctor by User ID
    public DoctorResponseDTO getDoctorByUserId(Long userId) {
        log.info("Fetching doctor profile for user: {}", userId);
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor profile not found for user: " + userId));
        return toDTO(doctor);
    }

}
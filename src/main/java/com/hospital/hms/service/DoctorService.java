package com.hospital.hms.service;

import com.hospital.hms.dto.request.DoctorOnboardRequestDTO;
import com.hospital.hms.dto.request.DoctorRequestDTO;
import com.hospital.hms.dto.response.DoctorResponseDTO;
import com.hospital.hms.model.*;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.repository.*;
import com.hospital.hms.repository.spec.DoctorSpecification;
import org.springframework.transaction.annotation.Transactional;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.data.domain.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
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

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    // ✅ UPDATED - added workStartTime, workEndTime, slotDurationMinutes
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
                doctor.getCreatedAt(),
                // ✅ NEW — 3 extra fields
                doctor.getWorkStartTime() != null
                        ? doctor.getWorkStartTime().toString()
                        : "09:00",
                doctor.getWorkEndTime() != null
                        ? doctor.getWorkEndTime().toString()
                        : "17:00",
                doctor.getSlotDurationMinutes() != null
                        ? doctor.getSlotDurationMinutes()
                        : 30
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

    // GET doctors by department with optional available filter
    public List<DoctorResponseDTO> getDoctorsByDepartment(
            Long departmentId, Boolean available) {
        log.info("Fetching doctors for department: {} available: {}",
                departmentId, available);

        List<Doctor> doctors;

        if (available != null && available) {
            // ✅ Filter by department AND available = true
            doctors = doctorRepository
                    .findByDepartmentIdAndAvailable(
                            departmentId, true);
        } else {
            // ✅ Return ALL doctors in department
            doctors = doctorRepository
                    .findByDepartmentId(departmentId);
        }

        return doctors.stream()
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

    // ✅ UPDATED - added workStartTime, workEndTime, slotDurationMinutes
    public DoctorResponseDTO createDoctor(DoctorRequestDTO dto) {
        log.info("Creating doctor profile for user: {}",
                dto.getUserId());

        User user = userRepository.findById(dto.getUserId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + dto.getUserId()));

        if (!user.getRole().name().equals("DOCTOR")) {
            throw new RuntimeException(
                    "User must have DOCTOR role!");
        }

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

        // ✅ NEW
        if (dto.getWorkStartTime() != null)
            doctor.setWorkStartTime(
                    LocalTime.parse(dto.getWorkStartTime()));
        if (dto.getWorkEndTime() != null)
            doctor.setWorkEndTime(
                    LocalTime.parse(dto.getWorkEndTime()));
        if (dto.getSlotDurationMinutes() != null)
            doctor.setSlotDurationMinutes(
                    dto.getSlotDurationMinutes());

        return toDTO(doctorRepository.save(doctor));
    }

    // ✅ UPDATED - added workStartTime, workEndTime, slotDurationMinutes
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

        // ✅ NEW
        if (dto.getWorkStartTime() != null)
            doctor.setWorkStartTime(
                    LocalTime.parse(dto.getWorkStartTime()));
        if (dto.getWorkEndTime() != null)
            doctor.setWorkEndTime(
                    LocalTime.parse(dto.getWorkEndTime()));
        if (dto.getSlotDurationMinutes() != null)
            doctor.setSlotDurationMinutes(
                    dto.getSlotDurationMinutes());

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

    // Search Functionality
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
                                "Doctor profile not found for user: "
                                        + userId));
        return toDTO(doctor);
    }

    // ✅ UPDATED - added workStartTime, workEndTime, slotDurationMinutes
    @Transactional
    public DoctorResponseDTO onboardDoctor(
            DoctorOnboardRequestDTO dto) {
        log.info("Onboarding new doctor: {}", dto.getEmail());

        if (userRepository.findByEmail(
                dto.getEmail()).isPresent()) {
            throw new RuntimeException(
                    "A user with this email already exists!");
        }

        Department department = departmentRepository
                .findById(dto.getDepartmentId())
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found: " +
                                        dto.getDepartmentId()));

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(
                passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(Role.DOCTOR);
        user = userRepository.save(user);

        Doctor doctor = new Doctor();
        doctor.setUser(user);
        doctor.setDepartment(department);
        doctor.setSpecialization(dto.getSpecialization());
        doctor.setExperienceYears(dto.getExperienceYears());
        doctor.setConsultationFee(dto.getConsultationFee());
        doctor.setBio(dto.getBio());
        doctor.setAvailable(true);

        // ✅ NEW
        if (dto.getWorkStartTime() != null)
            doctor.setWorkStartTime(
                    LocalTime.parse(dto.getWorkStartTime()));
        if (dto.getWorkEndTime() != null)
            doctor.setWorkEndTime(
                    LocalTime.parse(dto.getWorkEndTime()));
        if (dto.getSlotDurationMinutes() != null)
            doctor.setSlotDurationMinutes(
                    dto.getSlotDurationMinutes());

        doctor = doctorRepository.save(doctor);

        log.info("Doctor onboarded: userId={}, doctorId={}",
                user.getId(), doctor.getId());
        return toDTO(doctor);
    }

    // ✅ NEW - available slots logic moved to service
    public List<String> getAvailableSlots(
            Long doctorId, String date) {
        log.info("Fetching available slots for doctor {}" +
                " on {}", doctorId, date);

        Doctor doctor = doctorRepository.findById(doctorId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Doctor not found!"));

        LocalDate targetDate = LocalDate.parse(date);

        LocalTime start = doctor.getWorkStartTime() != null
                ? doctor.getWorkStartTime()
                : LocalTime.of(9, 0);
        LocalTime end = doctor.getWorkEndTime() != null
                ? doctor.getWorkEndTime()
                : LocalTime.of(17, 0);
        int duration = doctor.getSlotDurationMinutes() != null
                ? doctor.getSlotDurationMinutes()
                : 30;

        List<Appointment> existing = appointmentRepository
                .findByDoctorIdAndAppointmentDate(
                        doctorId, targetDate);

        Set<String> bookedTimes = existing.stream()
                .filter(a -> a.getStatus()
                        != AppointmentStatus.CANCELLED)
                .map(a -> a.getAppointmentTime()
                        .toString().substring(0, 5))
                .collect(Collectors.toSet());

        boolean isToday = targetDate.isEqual(LocalDate.now());
        LocalTime now = LocalTime.now();

        List<String> slots = new ArrayList<>();
        LocalTime cursor = start;

        while (cursor.isBefore(end)) {
            String slotStr = cursor.format(
                    DateTimeFormatter.ofPattern("HH:mm"));
            boolean isBooked = bookedTimes.contains(slotStr);
            boolean isPast = isToday && cursor.isBefore(now);

            if (!isBooked && !isPast) {
                slots.add(slotStr);
            }
            cursor = cursor.plusMinutes(duration);
        }

        return slots;
    }
}

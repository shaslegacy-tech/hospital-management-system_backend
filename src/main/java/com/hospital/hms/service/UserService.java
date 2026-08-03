package com.hospital.hms.service;

import com.hospital.hms.dto.request.ApprovePatientRequestDTO;
import com.hospital.hms.dto.response.PendingUserResponseDTO;
import com.hospital.hms.dto.response.UserSearchResponseDTO;
import com.hospital.hms.model.Patient;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.BloodGroup;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.model.enums.UserStatus;
import com.hospital.hms.repository.PatientRepository;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.service.email.EmailService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    // ─── Search user by email or phone ───────────────────────
    public UserSearchResponseDTO searchByEmailOrPhone(
            String query) {
        log.info("Searching user by email/phone: {}", query);

        User user = userRepository
                .findByEmailOrPhone(query)
                .orElseThrow(() ->
                        new RuntimeException(
                                "No user found with: " + query));

        boolean hasPatientRecord =
                patientRepository.existsByUserId(user.getId());

        return new UserSearchResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                user.getStatus(),
                hasPatientRecord
        );
    }

    // ─── Get all pending patients ─────────────────────────────
    public List<PendingUserResponseDTO> getPendingPatients() {
        log.info("Fetching all pending patients");

        return userRepository
                .findByRoleAndStatus(
                        Role.PATIENT,
                        UserStatus.PENDING)
                .stream()
                .map(u -> new PendingUserResponseDTO(
                        u.getId(),
                        u.getName(),
                        u.getEmail(),
                        u.getPhone(),
                        u.getCreatedAt() != null
                                ? u.getCreatedAt().toString()
                                : null
                ))
                .collect(Collectors.toList());
    }

    // ─── Get pending count for sidebar badge ──────────────────
    public long getPendingCount() {
        return userRepository.countByRoleAndStatus(
                Role.PATIENT, UserStatus.PENDING);
    }

    // ─── Approve patient ──────────────────────────────────────
    public String approvePatient(
            Long userId,
            ApprovePatientRequestDTO dto) {
        log.info("Approving patient userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + userId));

        // Validate user is a PATIENT
        if (user.getRole() != Role.PATIENT) {
            throw new RuntimeException(
                    "User is not a patient!");
        }

        // Check if already approved
        if (patientRepository.existsByUserId(userId)) {
            throw new RuntimeException(
                    "Patient record already exists!");
        }

        // Create Patient record
        Patient patient = new Patient();
        patient.setUser(user);
        patient.setDateOfBirth(
                LocalDate.parse(dto.getDateOfBirth()));
        patient.setBloodGroup(
                BloodGroup.valueOf(dto.getBloodGroup()));
        patient.setAddress(dto.getAddress());
        patient.setEmergencyContactName(
                dto.getEmergencyContactName());
        patient.setEmergencyContact(
                dto.getEmergencyContact());
        patient.setMedicalHistory(dto.getMedicalHistory());
        patientRepository.save(patient);

        // Activate the user
        user.setStatus(UserStatus.ACTIVE);
        userRepository.save(user);
        emailService.sendAccountApprovedEmail(user);
        // Notify patient — account approved
        notificationService.notify(
                user,
                "ACCOUNT_APPROVED",
                "Your account has been approved! " +
                        "You can now access all portal features.",
                "/dashboard"
        );

        log.info("Patient approved and activated: {}",
                user.getEmail());
        return "Patient approved successfully";
    }

    // ─── Reject patient ───────────────────────────────────────
    public String rejectPatient(Long userId) {
        log.info("Rejecting patient userId: {}", userId);

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new RuntimeException(
                                "User not found: " + userId));

        if (user.getRole() != Role.PATIENT) {
            throw new RuntimeException(
                    "User is not a patient!");
        }
        user.setStatus(UserStatus.REJECTED);
        user.setActive(false);
        userRepository.save(user);
        emailService.sendAccountRejectedEmail(user);

        // Notify patient — account rejected
        notificationService.notify(
                user,
                "ACCOUNT_REJECTED",
                "Your registration was not approved. " +
                        "Please contact the hospital for more information.",
                "/"
        );

        log.info("Patient rejected: {}", user.getEmail());
        return "Patient rejected successfully";
    }
}

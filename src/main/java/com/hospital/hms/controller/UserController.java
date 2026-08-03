// Add this to your existing UserController (or create one if you don't have it,
// at com/hospital/hms/controller/UserController.java)
//
// If you don't have a UserController yet, here's the full minimal version:

package com.hospital.hms.controller;

import com.hospital.hms.dto.request.ApprovePatientRequestDTO;
import com.hospital.hms.dto.response.PendingUserResponseDTO;
import com.hospital.hms.dto.response.UserSearchResponseDTO;
import com.hospital.hms.service.UserService;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.repository.UserRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.List;
import java.util.stream.Collectors;

@Tag(name = "Users", description = "User search and patient approval, Admin-only user lookups")
@Slf4j
@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Data
    public static class UserSummaryDTO {
        private Long id;
        private String name;
        private String email;
        private Role role;

        public UserSummaryDTO(Long id, String name, String email, Role role) {
            this.id = id;
            this.name = name;
            this.email = email;
            this.role = role;
        }
    }

    @Operation(
            summary = "Search user by email or phone",
            description = "Used by receptionist to check if " +
                    "a patient already has an account " +
                    "before creating a new one."
    )
    @GetMapping("/search")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<UserSearchResponseDTO> searchUser(
            @RequestParam String query) {
        return ResponseEntity.ok(
                userService.searchByEmailOrPhone(query));
    }

    @Operation(
            summary = "Get all pending patients",
            description = "Returns all self-registered patients " +
                    "waiting for receptionist approval."
    )
    @GetMapping("/pending-patients")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<List<PendingUserResponseDTO>>
    getPendingPatients() {
        return ResponseEntity.ok(
                userService.getPendingPatients());
    }

    @Operation(
            summary = "Get pending patient count",
            description = "Returns count of pending patients " +
                    "for sidebar badge display."
    )
    @GetMapping("/pending-patients/count")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<Map<String, Long>>
    getPendingCount() {
        return ResponseEntity.ok(
                Map.of("count",
                        userService.getPendingCount()));
    }

    @Operation(
            summary = "Approve a pending patient",
            description = "Completes the patient medical profile " +
                    "and activates their account."
    )
    @PostMapping("/{userId}/approve")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<String> approvePatient(
            @PathVariable Long userId,
            @Valid @RequestBody
            ApprovePatientRequestDTO dto) {
        return ResponseEntity.ok(
                userService.approvePatient(userId, dto));
    }

    @Operation(
            summary = "Reject a pending patient",
            description = "Rejects a self-registered patient " +
                    "and deactivates their account."
    )
    @PostMapping("/{userId}/reject")
    @PreAuthorize("hasAnyRole('ADMIN','RECEPTIONIST')")
    public ResponseEntity<String> rejectPatient(
            @PathVariable Long userId) {
        return ResponseEntity.ok(
                userService.rejectPatient(userId));
    }

    @Operation(
            summary = "List users by role",
            description = "Used by admin to pick a user when creating a Doctor " +
                    "profile, without needing to know raw user IDs."
    )
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserSummaryDTO>> getUsersByRole(
            @RequestParam(required = false) Role role) {
        log.info("Admin fetching users with role: {}", role);

        List<User> users = role != null
                ? userRepository.findByRole(role)
                : userRepository.findAll();

        List<UserSummaryDTO> result = users.stream()
                .map(u -> new UserSummaryDTO(u.getId(), u.getName(), u.getEmail(), u.getRole()))
                .collect(Collectors.toList());

        return ResponseEntity.ok(result);
    }
}

// Make sure UserRepository has this method (add if missing):
// List<User> findByRole(Role role);
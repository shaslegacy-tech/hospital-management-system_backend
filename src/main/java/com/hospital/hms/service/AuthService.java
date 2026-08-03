package com.hospital.hms.service;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AuthResponseDTO;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.Role;
import com.hospital.hms.model.enums.UserStatus;
import com.hospital.hms.repository.UserRepository;
import com.hospital.hms.security.JwtService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.security.core.userdetails.*;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class AuthService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Lazy
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    @Override
    public UserDetails loadUserByUsername(String email)
            throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "User not found: " + email));

        return org.springframework.security.core.userdetails
                .User.withUsername(user.getEmail())
                .password(user.getPassword())
                .roles(user.getRole().name())
                .build();
    }

    public AuthResponseDTO register(RegisterRequestDTO dto) {
        log.info("Registering user: {}", dto.getEmail());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new RuntimeException(
                    "Email already registered!");
        }

        User user = new User();
        user.setName(dto.getName());
        user.setEmail(dto.getEmail());
        user.setPassword(
                passwordEncoder.encode(dto.getPassword()));
        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setActive(true);

        // ✅ Self-registered PATIENTS start as PENDING
        // All other roles (ADMIN, DOCTOR, RECEPTIONIST)
        // are created directly as ACTIVE
        if (dto.getRole() == Role.PATIENT) {
            user.setStatus(UserStatus.PENDING);
        } else {
            user.setStatus(UserStatus.ACTIVE);
        }

        userRepository.save(user);
        log.info("User registered: {} with status: {}",
                user.getEmail(), user.getStatus());

        String token = jwtService.generateToken(
                user.getEmail(), user.getRole().name());

        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }

    public AuthResponseDTO login(LoginRequestDTO dto) {
        log.info("Login attempt: {}", dto.getEmail());

        User user = userRepository.findByEmail(dto.getEmail())
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        if (!user.isActive()) {
            throw new RuntimeException(
                    "Account is deactivated!");
        }

        // ✅ Block PENDING patients from logging in
        if (user.getStatus() == UserStatus.PENDING) {
            throw new RuntimeException(
                    "Your account is pending approval. " +
                            "Please wait for receptionist approval.");
        }

        // ✅ Block REJECTED patients from logging in
        if (user.getStatus() == UserStatus.REJECTED) {
            throw new RuntimeException(
                    "Your account has been rejected. " +
                            "Please contact the hospital.");
        }

        if (!passwordEncoder.matches(
                dto.getPassword(), user.getPassword())) {
            throw new RuntimeException("Invalid password!");
        }

        String token = jwtService.generateToken(
                user.getEmail(), user.getRole().name());

        log.info("Login successful: {}", user.getEmail());

        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );
    }

    public String changePassword(String email,
                                 ChangePasswordRequestDTO dto) {
        log.info("Password change requested by: {}", email);

        User user = userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new RuntimeException("User not found!"));

        if (!passwordEncoder.matches(
                dto.getCurrentPassword(), user.getPassword())) {
            throw new RuntimeException(
                    "Current password is incorrect!");
        }

        if (passwordEncoder.matches(
                dto.getNewPassword(), user.getPassword())) {
            throw new RuntimeException(
                    "New password must be different " +
                            "from current password!");
        }

        user.setPassword(
                passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);

        log.info("Password changed for: {}", user.getId());
        return "Password updated successfully";
    }
}

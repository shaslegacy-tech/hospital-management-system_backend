package com.hospital.hms.service;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AuthResponseDTO;
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.HospitalStatus;
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


    // ============================================================
    // USER DETAILS
    // ============================================================

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


    // ============================================================
    // REGISTER
    // ============================================================

    public AuthResponseDTO register(
            RegisterRequestDTO dto) {

        log.info(
                "Registering user: {}",
                dto.getEmail()
        );

        if (userRepository.existsByEmail(dto.getEmail())) {

            throw new RuntimeException(
                    "Email already registered!"
            );
        }

        User user = new User();

        user.setName(dto.getName());
        user.setEmail(dto.getEmail());

        user.setPassword(
                passwordEncoder.encode(
                        dto.getPassword()
                )
        );

        user.setPhone(dto.getPhone());
        user.setRole(dto.getRole());
        user.setActive(true);


        // ========================================================
        // PATIENT APPROVAL
        // ========================================================

        if (dto.getRole() == Role.PATIENT) {

            user.setStatus(
                    UserStatus.PENDING
            );

        } else {

            user.setStatus(
                    UserStatus.ACTIVE
            );
        }


        userRepository.save(user);

        log.info(
                "User registered: {} with status: {}",
                user.getEmail(),
                user.getStatus()
        );


        // ========================================================
        // GENERATE JWT
        // ========================================================

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole().name()
                );


        // ========================================================
        // HOSPITAL STATUS
        // ========================================================

        String hospitalStatus = null;

        if (user.getHospital() != null) {

            hospitalStatus =
                    user.getHospital()
                            .getStatus()
                            .toString();
        }


        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                hospitalStatus
        );
    }


    // ============================================================
    // LOGIN
    // ============================================================

    public AuthResponseDTO login(
            LoginRequestDTO dto) {

        log.info(
                "Login attempt: {}",
                dto.getEmail()
        );


        // ========================================================
        // FIND USER
        // ========================================================

        User user =
                userRepository
                        .findByEmail(dto.getEmail())
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found!"
                                )
                        );


        // ========================================================
        // USER ACTIVE CHECK
        // ========================================================

        if (!user.isActive()) {

            throw new RuntimeException(
                    "Account is deactivated!"
            );
        }


        // ========================================================
        // PATIENT USER STATUS CHECK
        // ========================================================

        // Block PENDING patients
        if (user.getStatus() == UserStatus.PENDING) {

            throw new RuntimeException(
                    "Your account is pending approval. " +
                    "Please wait for receptionist approval."
            );
        }


        // Block REJECTED patients
        if (user.getStatus() == UserStatus.REJECTED) {

            throw new RuntimeException(
                    "Your account has been rejected. " +
                    "Please contact the hospital."
            );
        }


        // ========================================================
        // PASSWORD VERIFICATION
        // ========================================================

        if (!passwordEncoder.matches(
                dto.getPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Invalid password!"
            );
        }


        // ========================================================
        // HOSPITAL STATUS CHECK
        // ========================================================

        String hospitalStatus = null;

        if (user.getHospital() != null) {

            Hospital hospital =
                    user.getHospital();


            // ----------------------------------------------------
            // REJECTED HOSPITAL
            // ----------------------------------------------------

            if (hospital.getStatus()
                    == HospitalStatus.REJECTED) {

                log.warn(
                        "Login blocked — hospital {} was rejected",
                        hospital.getId()
                );

                throw new RuntimeException(
                        "Your hospital's registration was not approved. " +
                        "Contact support for details."
                );
            }


            // ----------------------------------------------------
            // SUSPENDED HOSPITAL
            // ----------------------------------------------------

            if (hospital.getStatus()
                    == HospitalStatus.SUSPENDED) {

                log.warn(
                        "Login blocked — hospital {} is suspended",
                        hospital.getId()
                );

                throw new RuntimeException(
                        "Your hospital account has been suspended. " +
                        "Contact support to resolve this."
                );
            }


            // ----------------------------------------------------
            // PENDING / APPROVED
            // ----------------------------------------------------

            hospitalStatus =
                    hospital.getStatus().toString();
        }


        // ========================================================
        // GENERATE JWT
        // ========================================================

        String token =
                jwtService.generateToken(
                        user.getEmail(),
                        user.getRole().name()
                );


        log.info(
                "Login successful: {} | hospitalStatus: {}",
                user.getEmail(),
                hospitalStatus
        );


        // ========================================================
        // RESPONSE
        // ========================================================

        return new AuthResponseDTO(
                token,
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getPhone(),
                user.getRole(),
                hospitalStatus
        );
    }


    // ============================================================
    // CHANGE PASSWORD
    // ============================================================

    public String changePassword(
            String email,
            ChangePasswordRequestDTO dto) {

        log.info(
                "Password change requested by: {}",
                email
        );

        User user =
                userRepository.findByEmail(email)
                        .orElseThrow(() ->
                                new RuntimeException(
                                        "User not found!"
                                )
                        );


        if (!passwordEncoder.matches(
                dto.getCurrentPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "Current password is incorrect!"
            );
        }


        if (passwordEncoder.matches(
                dto.getNewPassword(),
                user.getPassword())) {

            throw new RuntimeException(
                    "New password must be different " +
                    "from current password!"
            );
        }


        user.setPassword(
                passwordEncoder.encode(
                        dto.getNewPassword()
                )
        );

        userRepository.save(user);


        log.info(
                "Password changed for: {}",
                user.getId()
        );


        return "Password updated successfully";
    }
}
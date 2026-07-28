package com.hospital.hms.controller;

import com.hospital.hms.dto.request.*;
import com.hospital.hms.dto.response.AuthResponseDTO;
import com.hospital.hms.service.AuthService;
import jakarta.validation.Valid;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Authentication",
        description = "Register and Login endpoints")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @Operation(
            summary = "Register a new user",
            description = "Creates a new user account with " +
                    "role ADMIN, DOCTOR, PATIENT, or " +
                    "RECEPTIONIST. Returns a JWT token."
    )
    @PostMapping("/register")
    public ResponseEntity<AuthResponseDTO> register(
            @Valid @RequestBody RegisterRequestDTO dto) {
        return ResponseEntity.ok(authService.register(dto));
    }

    @Operation(
            summary = "Login existing user",
            description = "Authenticates user with email " +
                    "and password. Returns a JWT token " +
                    "for subsequent requests."
    )
    @PostMapping("/login")
    public ResponseEntity<AuthResponseDTO> login(
            @Valid @RequestBody LoginRequestDTO dto) {
        return ResponseEntity.ok(authService.login(dto));
    }
}
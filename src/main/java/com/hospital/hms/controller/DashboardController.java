package com.hospital.hms.controller;

import com.hospital.hms.dto.response.DashboardResponseDTO;
import com.hospital.hms.service.DashboardService;

import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@Tag(
        name = "Dashboard",
        description = "Admin statistics and analytics"
)
@RestController
@RequestMapping("/api/dashboard")
public class DashboardController {

    @Autowired
    private DashboardService dashboardService;

    @GetMapping("/stats")
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    public ResponseEntity<DashboardResponseDTO> getDashboardStats(
            Authentication authentication) {

        return ResponseEntity.ok(
                dashboardService.getDashboardStats(authentication)
        );
    }
}
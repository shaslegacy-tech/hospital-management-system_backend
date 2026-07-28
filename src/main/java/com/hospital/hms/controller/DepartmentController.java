package com.hospital.hms.controller;

import com.hospital.hms.dto.request.DepartmentRequestDTO;
import com.hospital.hms.dto.response.DepartmentResponseDTO;
import com.hospital.hms.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "Departments",
        description = "Manage hospital departments")
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    @Autowired
    private DepartmentService departmentService;

    // GET /api/departments — all roles
    @GetMapping
    public ResponseEntity<List<DepartmentResponseDTO>>
    getAllDepartments() {
        return ResponseEntity.ok(
                departmentService.getAllDepartments());
    }

    // GET /api/departments/1
    @GetMapping("/{id}")
    public ResponseEntity<DepartmentResponseDTO>
    getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(
                departmentService.getDepartmentById(id));
    }

    // POST /api/departments — ADMIN only
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO>
    createDepartment(
            @Valid @RequestBody DepartmentRequestDTO dto) {
        return ResponseEntity.ok(
                departmentService.createDepartment(dto));
    }

    // PUT /api/departments/1 — ADMIN only
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO>
    updateDepartment(@PathVariable Long id,
                     @Valid @RequestBody DepartmentRequestDTO dto) {
        return ResponseEntity.ok(
                departmentService.updateDepartment(id, dto));
    }

    // DELETE /api/departments/1 — ADMIN only
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDepartment(
            @PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.ok(
                "Department deactivated successfully");
    }
}
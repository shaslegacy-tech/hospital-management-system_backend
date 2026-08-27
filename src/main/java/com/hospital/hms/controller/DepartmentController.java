package com.hospital.hms.controller;
 
import com.hospital.hms.dto.request.DepartmentRequestDTO;
import com.hospital.hms.dto.response.DepartmentResponseDTO;
import com.hospital.hms.model.Department;
import com.hospital.hms.model.Hospital;
import com.hospital.hms.repository.DepartmentRepository;
import com.hospital.hms.service.HospitalContextService;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.util.List;
import java.util.stream.Collectors;
 
@Tag(name = "Departments")
@Slf4j
@RestController
@RequestMapping("/api/departments")
public class DepartmentController {
 
    @Autowired private DepartmentRepository departmentRepository;
    @Autowired private HospitalContextService hospitalContextService;
 
    private DepartmentResponseDTO toDTO(Department d) {
        return new DepartmentResponseDTO(
            d.getId(), d.getName(), d.getDescription(), true,
            d.getCreatedAt()
        );
    }
 
    @GetMapping
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<DepartmentResponseDTO>> getDepartments(
            @RequestParam(required = false) Long hospitalId,
            Authentication authentication) {
 
        Long targetHospitalId = hospitalId;
 
        // If the caller is hospital staff (admin/receptionist/doctor) and
        // didn't explicitly pass a hospitalId, default to their own hospital.
        if (targetHospitalId == null) {
            try {
                targetHospitalId = hospitalContextService
                    .getCurrentUserHospital(authentication).getId();
            } catch (Exception ignored) {
                // Caller is a PATIENT (no hospital of their own) and didn't
                // specify one — return empty rather than leaking everything.
                return ResponseEntity.ok(List.of());
            }
        }
        final Long resolvedHospitalId = targetHospitalId;
 
        List<Department> departments = departmentRepository.findAll().stream()
            .filter(d -> d.getHospital().getId().equals(resolvedHospitalId))
            .collect(Collectors.toList());
 
        return ResponseEntity.ok(
            departments.stream().map(this::toDTO).collect(Collectors.toList()));
    }
 
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> createDepartment(
            @Valid @RequestBody DepartmentRequestDTO dto,
            Authentication authentication) {
        log.info("Creating department: {}", dto.getName());
 
        Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
 
        Department department = new Department();
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
        department.setHospital(hospital); // scoped automatically — admin
                                           // can never create a department
                                           // for someone else's hospital
 
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toDTO(departmentRepository.save(department)));
    }
 
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<DepartmentResponseDTO> updateDepartment(
            @PathVariable Long id,
            @Valid @RequestBody DepartmentRequestDTO dto,
            Authentication authentication) {
 
        Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found!"));
 
        // The critical check — this admin can only edit THEIR hospital's
        // department, even if they somehow guess another department's ID.
        if (!department.getHospital().getId().equals(hospital.getId())) {
            throw new RuntimeException("You don't have access to this department!");
        }
 
        department.setName(dto.getName());
        department.setDescription(dto.getDescription());
 
        return ResponseEntity.ok(toDTO(departmentRepository.save(department)));
    }
 
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<String> deleteDepartment(
            @PathVariable Long id, Authentication authentication) {
 
        Hospital hospital = hospitalContextService.getCurrentUserHospital(authentication);
        Department department = departmentRepository.findById(id)
            .orElseThrow(() -> new RuntimeException("Department not found!"));
 
        if (!department.getHospital().getId().equals(hospital.getId())) {
            throw new RuntimeException("You don't have access to this department!");
        }
 
        departmentRepository.deleteById(id);
        return ResponseEntity.ok("Department deleted successfully");
    }
}
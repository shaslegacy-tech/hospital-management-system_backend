package com.hospital.hms.service;

import com.hospital.hms.dto.request.DepartmentRequestDTO;
import com.hospital.hms.dto.response.DepartmentResponseDTO;
import com.hospital.hms.model.Department;
import com.hospital.hms.repository.DepartmentRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DepartmentService {

    @Autowired
    private DepartmentRepository departmentRepository;

    private DepartmentResponseDTO toDTO(Department dept) {
        return new DepartmentResponseDTO(
                dept.getId(),
                dept.getName(),
                dept.getDescription(),
                dept.isActive(),
                dept.getCreatedAt()
        );
    }

    // GET all active departments
    public List<DepartmentResponseDTO> getAllDepartments() {
        log.info("Fetching all departments");
        return departmentRepository.findByActiveTrue()
                .stream()
                .map(this::toDTO)
                .collect(Collectors.toList());
    }

    // GET department by ID
    public DepartmentResponseDTO getDepartmentById(Long id) {
        Department dept = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found: " + id));
        return toDTO(dept);
    }

    // POST - create department
    public DepartmentResponseDTO createDepartment(
            DepartmentRequestDTO dto) {
        log.info("Creating department: {}", dto.getName());

        if (departmentRepository.existsByName(dto.getName())) {
            throw new RuntimeException(
                    "Department already exists: " + dto.getName());
        }

        Department dept = new Department();
        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());
        dept.setActive(true);

        return toDTO(departmentRepository.save(dept));
    }

    // PUT - update department
    public DepartmentResponseDTO updateDepartment(
            Long id, DepartmentRequestDTO dto) {
        log.info("Updating department: {}", id);

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found: " + id));

        dept.setName(dto.getName());
        dept.setDescription(dto.getDescription());

        return toDTO(departmentRepository.save(dept));
    }

    // DELETE - soft delete
    public void deleteDepartment(Long id) {
        log.warn("Deleting department: {}", id);

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() ->
                        new RuntimeException(
                                "Department not found: " + id));

        dept.setActive(false);  // soft delete
        departmentRepository.save(dept);
    }
}
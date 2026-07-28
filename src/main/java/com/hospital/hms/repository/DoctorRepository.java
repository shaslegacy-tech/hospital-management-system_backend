package com.hospital.hms.repository;

import com.hospital.hms.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface DoctorRepository
        extends JpaRepository<Doctor, Long>, JpaSpecificationExecutor<Doctor> {

    List<Doctor> findByDepartmentId(Long departmentId);
    List<Doctor> findByAvailableTrue();
    Optional<Doctor> findByUserId(Long userId);
    List<Doctor> findBySpecializationContainingIgnoreCase(
            String specialization);
}
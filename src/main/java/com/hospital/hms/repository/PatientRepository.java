package com.hospital.hms.repository;

import com.hospital.hms.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository
        extends JpaRepository<Patient, Long>,
        JpaSpecificationExecutor<Patient> {

    boolean existsByUserId(Long userId);

    Optional<Patient> findByUserId(Long userId);
}
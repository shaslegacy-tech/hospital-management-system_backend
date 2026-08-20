package com.hospital.hms.repository;

import com.hospital.hms.model.PatientFile;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PatientFileRepository
        extends JpaRepository<PatientFile, Long> {

    List<PatientFile> findByPatientId(Long patientId);

    List<PatientFile> findByPatientIdAndFileType(
            Long patientId,
            String fileType
    );

    Optional<PatientFile> findByFileName(String fileName);
}
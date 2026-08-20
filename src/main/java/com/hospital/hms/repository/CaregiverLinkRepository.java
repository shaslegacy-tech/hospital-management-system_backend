package com.hospital.hms.repository;

import com.hospital.hms.model.CaregiverLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface CaregiverLinkRepository
        extends JpaRepository<CaregiverLink, Long> {

    
    List<CaregiverLink> findByCaregiverId(Long caregiverUserId);

   
    List<CaregiverLink> findByPatientId(Long patientId);

    
    boolean existsByCaregiverIdAndPatientId(
            Long caregiverUserId,
            Long patientId
    );

    
    boolean existsByCaregiverIdAndPatientIdAndActiveTrue(
            Long caregiverUserId,
            Long patientId
    );

    
    Optional<CaregiverLink> findByCaregiverIdAndPatientId(
            Long caregiverUserId,
            Long patientId
    );

    
    Optional<CaregiverLink> findByCaregiverIdAndPatientIdAndActiveTrue(
            Long caregiverUserId,
            Long patientId
    );

    
    List<CaregiverLink> findByCaregiverIdAndActiveTrue(
            Long caregiverUserId
    );

    
    List<CaregiverLink> findByPatientIdAndActiveTrue(
            Long patientId
    );
}
package com.hospital.hms.repository;
 
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.enums.HospitalStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {
    List<Hospital> findByStatus(HospitalStatus status);
}
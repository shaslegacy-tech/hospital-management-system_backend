package com.hospital.hms.repository;

import com.hospital.hms.model.Bill;
import com.hospital.hms.model.enums.BillStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BillRepository
        extends JpaRepository<Bill, Long> {

    Optional<Bill> findByAppointmentId(Long appointmentId);

    List<Bill> findByAppointmentPatientId(Long patientId);

    List<Bill> findByStatus(BillStatus status);

    // Total revenue
    @Query("SELECT SUM(b.totalAmount) FROM Bill b " +
            "WHERE b.status = 'PAID'")
    Double getTotalRevenue();

    // Count by status
    Long countByStatus(BillStatus status);
}
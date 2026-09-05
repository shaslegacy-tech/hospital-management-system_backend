package com.hospital.hms.repository;
 
import com.hospital.hms.model.SubscriptionPayment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
 
import java.util.List;
 
@Repository
public interface SubscriptionPaymentRepository extends JpaRepository<SubscriptionPayment, Long> {
    List<SubscriptionPayment> findByHospitalIdOrderByCreatedAtDesc(Long hospitalId);
}
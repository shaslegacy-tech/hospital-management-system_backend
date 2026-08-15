package com.hospital.hms.repository;
 
import com.hospital.hms.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
 
import java.util.List;
import java.util.Optional;
 
@Repository
public interface ReviewRepository extends JpaRepository<Review, Long> {
 
    List<Review> findByDoctorIdOrderByCreatedAtDesc(Long doctorId);
 
    Optional<Review> findByAppointmentId(Long appointmentId);
 
    boolean existsByAppointmentId(Long appointmentId);
 
    @Query("SELECT AVG(r.rating) FROM Review r WHERE r.doctor.id = :doctorId")
    Double findAverageRatingByDoctorId(@Param("doctorId") Long doctorId);
 
    long countByDoctorId(Long doctorId);
}
package com.hospital.hms.controller;
 
import com.hospital.hms.dto.request.ReviewRequestDTO;
import com.hospital.hms.dto.response.ReviewResponseDTO;
import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Review;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.ReviewRepository;
import com.hospital.hms.repository.UserRepository;
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
 
@Tag(name = "Reviews", description = "Doctor ratings and reviews")
@Slf4j
@RestController
@RequestMapping("/api/reviews")
public class ReviewController {
 
    @Autowired
    private ReviewRepository reviewRepository;
 
    @Autowired
    private AppointmentRepository appointmentRepository;
 
    @Autowired
    private UserRepository userRepository;
 
    private ReviewResponseDTO toDTO(Review r) {
        return new ReviewResponseDTO(
            r.getId(),
            r.getAppointment().getId(),
            r.getPatient().getUser().getName(),
            r.getDoctor().getId(),
            r.getRating(),
            r.getComment(),
            r.getCreatedAt() != null ? r.getCreatedAt().toString() : null
        );
    }
 
    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<ReviewResponseDTO> createReview(
            @Valid @RequestBody ReviewRequestDTO dto,
            Authentication authentication) {
        log.info("Creating review for appointment {}", dto.getAppointmentId());
 
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found!"));
 
        Appointment appointment = appointmentRepository.findById(dto.getAppointmentId())
            .orElseThrow(() -> new RuntimeException("Appointment not found!"));
 
        // Make sure this appointment actually belongs to the logged-in patient
        if (!appointment.getPatient().getUser().getId().equals(user.getId())) {
            throw new RuntimeException("You can only review your own appointments!");
        }
 
        if (appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new RuntimeException("You can only review completed visits!");
        }
 
        if (reviewRepository.existsByAppointmentId(appointment.getId())) {
            throw new RuntimeException("You've already reviewed this visit!");
        }
 
        Review review = new Review();
        review.setAppointment(appointment);
        review.setPatient(appointment.getPatient());
        review.setDoctor(appointment.getDoctor());
        review.setRating(dto.getRating());
        review.setComment(dto.getComment());
 
        log.info("Review created for doctor {} by patient {}",
            appointment.getDoctor().getId(), appointment.getPatient().getId());
 
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(toDTO(reviewRepository.save(review)));
    }
 
    @GetMapping("/doctor/{doctorId}")
    @PreAuthorize("hasAnyRole('ADMIN','DOCTOR','PATIENT','RECEPTIONIST')")
    public ResponseEntity<List<ReviewResponseDTO>> getDoctorReviews(
            @PathVariable Long doctorId) {
        List<ReviewResponseDTO> result = reviewRepository
            .findByDoctorIdOrderByCreatedAtDesc(doctorId)
            .stream()
            .map(this::toDTO)
            .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }
 
    @GetMapping("/appointment/{appointmentId}/exists")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<Boolean> reviewExists(@PathVariable Long appointmentId) {
        return ResponseEntity.ok(reviewRepository.existsByAppointmentId(appointmentId));
    }
}
 
package com.hospital.hms.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "reviews")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Review extends BaseEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @OneToOne
    @JoinColumn(name = "appointment_id", nullable = false, unique = true)
    private Appointment appointment;
 
    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;
 
    @ManyToOne
    @JoinColumn(name = "doctor_id", nullable = false)
    private Doctor doctor;
 
    private Integer rating; // 1-5
 
    @Column(length = 1000)
    private String comment;
}
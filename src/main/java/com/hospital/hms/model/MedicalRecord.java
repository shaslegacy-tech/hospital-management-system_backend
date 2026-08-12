package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "medical_records")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class MedicalRecord extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    @Column(length = 1000)
    private String diagnosis;

    @Column(length = 1000)
    private String treatment;

    @Column(length = 1000)
    private String notes;

    @Column(columnDefinition = "TEXT")
    private String patientSummary;
}
package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "prescriptions")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Prescription extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "medical_record_id")
    private MedicalRecord medicalRecord;

    @Column(nullable = false)
    private String medicineName;

    private String dosage;

    private String duration;

    private String instructions;
}
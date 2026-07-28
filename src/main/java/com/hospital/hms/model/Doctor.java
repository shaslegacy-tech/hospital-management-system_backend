package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "doctors")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Doctor extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    @ManyToOne
    @JoinColumn(name = "department_id")
    private Department department;

    private String specialization;

    private Integer experienceYears;

    private Double consultationFee;

    @Column(length = 1000)
    private String bio;

    private boolean available = true;
}
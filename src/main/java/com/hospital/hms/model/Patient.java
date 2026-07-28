package com.hospital.hms.model;

import com.hospital.hms.model.enums.BloodGroup;
import jakarta.persistence.*;
import lombok.*;
import java.time.LocalDate;

@Entity
@Table(name = "patients")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Patient extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private LocalDate dateOfBirth;

    @Enumerated(EnumType.STRING)
    private BloodGroup bloodGroup;

    private String address;

    private String emergencyContact;

    private String emergencyContactName;

    @Column(length = 2000)
    private String medicalHistory;
}
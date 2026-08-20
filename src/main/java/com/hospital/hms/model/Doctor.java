package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;
import java.time.LocalTime;

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

    private String gender;

    @Column(length = 1000)
    private String bio;

    private boolean available = true;

    @Column(name = "work_start_time")
    private LocalTime workStartTime = LocalTime.of(9, 0);

    @Column(name = "work_end_time")
    private LocalTime workEndTime = LocalTime.of(17, 0);

    @Column(name = "slot_duration_minutes")
    private Integer slotDurationMinutes = 30;
}
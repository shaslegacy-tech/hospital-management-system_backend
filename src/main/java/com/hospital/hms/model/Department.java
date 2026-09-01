package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
    name = "departments",
    uniqueConstraints = {
        @UniqueConstraint(
            name = "uk_department_hospital_name",
            columnNames = {"hospital_id", "name"}
        )
    }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Department extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    private String description;

    private boolean active = true;

    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
}
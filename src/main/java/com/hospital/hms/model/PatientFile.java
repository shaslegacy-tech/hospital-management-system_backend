package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "patient_files")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class PatientFile extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "patient_id", nullable = false)
    private Patient patient;

    private String fileName;

    private String originalFileName;

    private String fileType;   // REPORT, XRAY, PRESCRIPTION

    private String contentType; // application/pdf, image/png

    private String filePath;

    private Long fileSize;

    private String description;
}
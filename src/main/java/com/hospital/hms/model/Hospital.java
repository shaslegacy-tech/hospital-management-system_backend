package com.hospital.hms.model;
 
import com.hospital.hms.model.enums.HospitalStatus;
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "hospitals")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Hospital extends BaseEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    private String name;
 
    @Column(length = 500)
    private String address;
 
    private String city;
    private String state;
    private String pincode;
 
    private Double latitude;
    private Double longitude;
 
    private String contactPhone;
    private String contactEmail;
 
    @Column(length = 1000)
    private String description;
 
    private String logoUrl;
 
    @Enumerated(EnumType.STRING)
    private HospitalStatus status = HospitalStatus.PENDING;
}
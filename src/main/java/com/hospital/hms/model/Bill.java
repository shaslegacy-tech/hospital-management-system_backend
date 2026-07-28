package com.hospital.hms.model;

import com.hospital.hms.model.enums.BillStatus;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "bills")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Bill extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne
    @JoinColumn(name = "appointment_id", unique = true)
    private Appointment appointment;

    private Double consultationFee;

    private Double additionalCharges;

    private Double totalAmount;

    @Enumerated(EnumType.STRING)
    private BillStatus status = BillStatus.PENDING;

    private String paymentMethod;

    private String notes;
}
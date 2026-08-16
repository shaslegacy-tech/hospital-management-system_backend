package com.hospital.hms.model;
 
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "audit_logs")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuditLog extends BaseEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "user_id")
    private User user; // nullable — some actions might not have a clear actor
 
    private String action;     // e.g. "APPOINTMENT_BOOKED", "BILL_PAID"
    private String entityType; // e.g. "Appointment", "Bill", "Patient"
    private Long entityId;
 
    @Column(length = 500)
    private String details;    // human-readable summary of what happened
}
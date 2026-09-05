package com.hospital.hms.model;
 
import com.hospital.hms.model.enums.SubscriptionPaymentStatus;
import com.hospital.hms.model.enums.SubscriptionPlan;
import jakarta.persistence.*;
import lombok.*;
 
@Entity
@Table(name = "subscription_payments")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class SubscriptionPayment extends BaseEntity {
 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
 
    @ManyToOne
    @JoinColumn(name = "hospital_id", nullable = false)
    private Hospital hospital;
 
    @Enumerated(EnumType.STRING)
    private SubscriptionPlan plan;
 
    private Double amount;
 
    private String razorpayOrderId;
    private String razorpayPaymentId;
 
    @Enumerated(EnumType.STRING)
    private SubscriptionPaymentStatus status = SubscriptionPaymentStatus.PENDING;
}
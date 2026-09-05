package com.hospital.hms.controller;
 
import com.hospital.hms.dto.request.SubscriptionPaymentVerifyDTO;
import com.hospital.hms.dto.response.SubscriptionPaymentOrderDTO;
import com.hospital.hms.dto.response.SubscriptionResponseDTO;
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.SubscriptionPayment;
import com.hospital.hms.model.enums.SubscriptionPaymentStatus;
import com.hospital.hms.model.enums.SubscriptionPlan;
import com.hospital.hms.repository.HospitalRepository;
import com.hospital.hms.repository.SubscriptionPaymentRepository;
import com.hospital.hms.service.HospitalContextService;
import com.hospital.hms.service.SubscriptionPlanConfig;
import com.razorpay.Order;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
 
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
 
@Tag(name = "Subscriptions", description = "Hospital SaaS billing")
@Slf4j
@RestController
@RequestMapping("/api/subscriptions")
public class SubscriptionController {
 
    @Autowired private HospitalRepository hospitalRepository;
    @Autowired private SubscriptionPaymentRepository paymentRepository;
    @Autowired private HospitalContextService hospitalContextService;
    @Autowired private SubscriptionPlanConfig planConfig;
 
    @Value("${razorpay.key-id}") private String keyId;
    @Value("${razorpay.key-secret}") private String keySecret;
 
    private SubscriptionResponseDTO toDTO(Hospital h) {
        boolean active = h.getSubscriptionExpiresAt() != null
            && h.getSubscriptionExpiresAt().isAfter(LocalDateTime.now());
        return new SubscriptionResponseDTO(
            h.getSubscriptionPlan() != null ? h.getSubscriptionPlan().toString() : "NONE",
            h.getSubscriptionExpiresAt() != null ? h.getSubscriptionExpiresAt().toString() : null,
            active,
            h.getSubscriptionPlan() != null ? planConfig.getMonthlyPrice(h.getSubscriptionPlan()) : 0
        );
    }
 
    @GetMapping("/my-hospital")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDTO> getMySubscription(Authentication auth) {
        Hospital hospital = hospitalContextService.getCurrentUserHospital(auth);
        return ResponseEntity.ok(toDTO(hospital));
    }
 
    @PostMapping("/create-order")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionPaymentOrderDTO> createOrder(
            @RequestParam String plan, Authentication auth) throws Exception {
 
        SubscriptionPlan targetPlan = SubscriptionPlan.valueOf(plan.toUpperCase());
        double price = planConfig.getMonthlyPrice(targetPlan);
        if (price <= 0) {
            throw new RuntimeException("Invalid plan for payment: " + plan);
        }
 
        long amountInPaise = Math.round(price * 100);
 
        RazorpayClient client = new RazorpayClient(keyId, keySecret);
        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "subscription_" + System.currentTimeMillis());
 
        Order order = client.orders.create(orderRequest);
 
        return ResponseEntity.ok(new SubscriptionPaymentOrderDTO(
            order.get("id"), amountInPaise, "INR", keyId));
    }
 
    @PostMapping("/verify")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<SubscriptionResponseDTO> verifyPayment(
            @Valid @RequestBody SubscriptionPaymentVerifyDTO dto,
            Authentication auth) throws Exception {
 
        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id", dto.getRazorpayOrderId());
        attributes.put("razorpay_payment_id", dto.getRazorpayPaymentId());
        attributes.put("razorpay_signature", dto.getRazorpaySignature());
 
        if (!Utils.verifyPaymentSignature(attributes, keySecret)) {
            throw new RuntimeException("Payment verification failed!");
        }
 
        Hospital hospital = hospitalContextService.getCurrentUserHospital(auth);
        SubscriptionPlan plan = SubscriptionPlan.valueOf(dto.getPlan().toUpperCase());
        double price = planConfig.getMonthlyPrice(plan);
 
        // Extend from current expiry if still active, otherwise from now —
        // so renewing early doesn't waste remaining paid days.
        LocalDateTime base = (hospital.getSubscriptionExpiresAt() != null
            && hospital.getSubscriptionExpiresAt().isAfter(LocalDateTime.now()))
            ? hospital.getSubscriptionExpiresAt()
            : LocalDateTime.now();
 
        hospital.setSubscriptionPlan(plan);
        hospital.setSubscriptionExpiresAt(base.plusDays(30));
        hospitalRepository.save(hospital);
 
        SubscriptionPayment payment = new SubscriptionPayment();
        payment.setHospital(hospital);
        payment.setPlan(plan);
        payment.setAmount(price);
        payment.setRazorpayOrderId(dto.getRazorpayOrderId());
        payment.setRazorpayPaymentId(dto.getRazorpayPaymentId());
        payment.setStatus(SubscriptionPaymentStatus.SUCCESS);
        paymentRepository.save(payment);
 
        log.info("Hospital {} subscribed to {} until {}",
            hospital.getId(), plan, hospital.getSubscriptionExpiresAt());
 
        return ResponseEntity.ok(toDTO(hospital));
    }
 
    @GetMapping("/history")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<SubscriptionPayment>> getHistory(Authentication auth) {
        Hospital hospital = hospitalContextService.getCurrentUserHospital(auth);
        return ResponseEntity.ok(
            paymentRepository.findByHospitalIdOrderByCreatedAtDesc(hospital.getId()));
        // Fine to return the entity directly for an internal history view;
        // swap for a proper DTO if you want to control the exact shape.
    }
}
 
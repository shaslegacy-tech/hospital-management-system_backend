package com.hospital.hms.controller;

import com.hospital.hms.dto.request.PaymentVerifyRequestDTO;
import com.hospital.hms.dto.response.PaymentOrderResponseDTO;
import com.hospital.hms.model.Bill;
import com.hospital.hms.model.enums.BillStatus;
import com.hospital.hms.repository.BillRepository;
import com.hospital.hms.service.NotificationService;
import com.razorpay.Order;
import com.hospital.hms.service.email.EmailService;
import com.razorpay.RazorpayClient;
import com.razorpay.Utils;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Payments",
        description = "Razorpay order creation and verification")
@Slf4j
@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private EmailService emailService;

    @Value("${razorpay.key-id}")
    private String keyId;

    @Value("${razorpay.key-secret}")
    private String keySecret;

    @Operation(
            summary = "Create a Razorpay order for a bill",
            description = "Called when the patient clicks 'Pay now'. Creates a " +
                    "Razorpay order for the bill's pending amount, which " +
                    "the frontend uses to open the Razorpay checkout."
    )
    @PostMapping("/create-order/{billId}")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN','RECEPTIONIST')")
    public ResponseEntity<PaymentOrderResponseDTO> createOrder(
            @PathVariable Long billId) throws Exception {
        log.info("Creating Razorpay order for bill {}", billId);

        Bill bill = billRepository.findById(billId)
                .orElseThrow(() ->
                        new RuntimeException("Bill not found!"));

        if (bill.getStatus() == BillStatus.PAID) {
            throw new RuntimeException(
                    "This bill is already paid!");
        }

        long amountInPaise =
                Math.round(bill.getTotalAmount() * 100);

        RazorpayClient client =
                new RazorpayClient(keyId, keySecret);

        JSONObject orderRequest = new JSONObject();
        orderRequest.put("amount", amountInPaise);
        orderRequest.put("currency", "INR");
        orderRequest.put("receipt", "bill_" + bill.getId());

        Order order = client.orders.create(orderRequest);

        log.info("Razorpay order {} created for bill {}",
                order.get("id"), billId);

        return ResponseEntity.ok(new PaymentOrderResponseDTO(
                order.get("id"),
                amountInPaise,
                "INR",
                keyId,
                bill.getId()
        ));
    }

    @Operation(
            summary = "Verify a Razorpay payment and mark the bill as paid",
            description = "Called after the Razorpay checkout succeeds on the " +
                    "frontend. Verifies the payment signature server-side " +
                    "before trusting it, then marks the bill PAID."
    )
    @PostMapping("/verify")
    @PreAuthorize("hasAnyRole('PATIENT','ADMIN','RECEPTIONIST')")
    public ResponseEntity<String> verifyPayment(
            @Valid @RequestBody PaymentVerifyRequestDTO dto)
            throws Exception {
        log.info("Verifying payment for bill {}",
                dto.getBillId());

        JSONObject attributes = new JSONObject();
        attributes.put("razorpay_order_id",
                dto.getRazorpayOrderId());
        attributes.put("razorpay_payment_id",
                dto.getRazorpayPaymentId());
        attributes.put("razorpay_signature",
                dto.getRazorpaySignature());

        boolean isValid = Utils.verifyPaymentSignature(
                attributes, keySecret);

        if (!isValid) {
            log.warn("Payment signature verification FAILED for bill {}",
                    dto.getBillId());
            throw new RuntimeException(
                    "Payment verification failed!");
        }

        Bill bill = billRepository.findById(dto.getBillId())
                .orElseThrow(() ->
                        new RuntimeException("Bill not found!"));

        bill.setStatus(BillStatus.PAID);
        bill.setPaymentMethod("RAZORPAY");
        Bill saved = billRepository.save(bill);
        emailService.sendPaymentReceivedEmail(saved);

        // Notify patient — Razorpay payment received
        notificationService.notify(
                saved.getAppointment()
                        .getPatient().getUser(),
                "PAYMENT_RECEIVED",
                "Payment of ₹" +
                        saved.getTotalAmount() +
                        " received successfully via Razorpay",
                "/bills"
        );

        log.info("Bill {} marked PAID via Razorpay payment {}, notification sent!",
                dto.getBillId(), dto.getRazorpayPaymentId());

        return ResponseEntity.ok(
                "Payment verified and bill marked as paid");
    }
}

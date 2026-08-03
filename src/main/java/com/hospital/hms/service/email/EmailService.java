package com.hospital.hms.service.email;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Bill;
import com.hospital.hms.model.MedicalRecord;
import com.hospital.hms.model.User;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private final String fromEmail =
            "noreply@aarogyaai.com";

    // ─── Core send method ─────────────────────────────────
    @Async
    public void sendEmail(
            String toEmail,
            String subject,
            String htmlBody) {
        try {
            MimeMessage message =
                    mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(
                            message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true);

            mailSender.send(message);
            log.info("✅ Email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("❌ Failed to send email to {}: {}",
                    toEmail, e.getMessage());
        }
    }

    // ─── 1. Appointment booked ────────────────────────────
    @Async
    public void sendAppointmentBookedEmail(
            Appointment appointment) {
        String email = appointment.getPatient()
                .getUser().getEmail();
        sendEmail(
                email,
                "✅ Appointment Booked — " +
                        appointment.getAppointmentDate(),
                EmailTemplates.appointmentBooked(
                        appointment)
        );
        log.info("Appointment booked email → {}",
                email);
    }

    // ─── 2. Appointment confirmed ─────────────────────────
    @Async
    public void sendAppointmentConfirmedEmail(
            Appointment appointment) {
        String email = appointment.getPatient()
                .getUser().getEmail();
        sendEmail(
                email,
                "✅ Appointment Confirmed — " +
                        appointment.getAppointmentDate(),
                EmailTemplates.appointmentConfirmed(
                        appointment)
        );
        log.info("Appointment confirmed email → {}",
                email);
    }

    // ─── 3. Appointment cancelled ─────────────────────────
    @Async
    public void sendAppointmentCancelledEmail(
            Appointment appointment) {
        String email = appointment.getPatient()
                .getUser().getEmail();
        sendEmail(
                email,
                "❌ Appointment Cancelled",
                EmailTemplates.appointmentCancelled(
                        appointment)
        );
        log.info("Appointment cancelled email → {}",
                email);
    }

    // ─── 4. Bill generated ────────────────────────────────
    @Async
    public void sendBillGeneratedEmail(Bill bill) {
        String email = bill.getAppointment()
                .getPatient().getUser().getEmail();
        sendEmail(
                email,
                "🧾 Bill Generated — ₹" +
                        bill.getTotalAmount(),
                EmailTemplates.billGenerated(bill)
        );
        log.info("Bill generated email → {}", email);
    }

    // ─── 5. Payment received ✅ NEW ───────────────────────
    @Async
    public void sendPaymentReceivedEmail(Bill bill) {
        String email = bill.getAppointment()
                .getPatient().getUser().getEmail();
        sendEmail(
                email,
                "💚 Payment Received — ₹" +
                        bill.getTotalAmount(),
                EmailTemplates.paymentReceived(bill)
        );
        log.info("Payment received email → {}", email);
    }

    // ─── 6. Account approved ✅ NEW ───────────────────────
    @Async
    public void sendAccountApprovedEmail(User user) {
        sendEmail(
                user.getEmail(),
                "🎉 Your Account Has Been Approved",
                EmailTemplates.accountApproved(user)
        );
        log.info("Account approved email → {}",
                user.getEmail());
    }

    // ─── 7. Account rejected ✅ NEW ───────────────────────
    @Async
    public void sendAccountRejectedEmail(User user) {
        sendEmail(
                user.getEmail(),
                "Account Registration Update",
                EmailTemplates.accountRejected(user)
        );
        log.info("Account rejected email → {}",
                user.getEmail());
    }

    // ─── 8. Medical record added ✅ NEW ───────────────────
    @Async
    public void sendMedicalRecordAddedEmail(
            MedicalRecord record) {
        String email = record.getAppointment()
                .getPatient().getUser().getEmail();
        sendEmail(
                email,
                "📋 Medical Record Added",
                EmailTemplates.medicalRecordAdded(
                        record.getAppointment(),
                        record.getDiagnosis())
        );
        log.info("Medical record email → {}", email);
    }
}

package com.hospital.hms.service.email;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Bill;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    private String fromEmail = "noreply@hospital.com";

    // Core send method
    @Async  // sends email in background thread
    public void sendEmail(String toEmail,
                          String subject,
                          String htmlBody) {
        try {
            MimeMessage message =
                    mailSender.createMimeMessage();
            MimeMessageHelper helper =
                    new MimeMessageHelper(message, true,
                            "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject(subject);
            helper.setText(htmlBody, true); // true = HTML

            mailSender.send(message);
            log.info("Email sent to: {}", toEmail);

        } catch (MessagingException e) {
            log.error("Failed to send email to {}: {}",
                    toEmail, e.getMessage());
        }
    }

    // Appointment booked
    @Async
    public void sendAppointmentBookedEmail(
            Appointment appointment) {
        String email = appointment.getPatient()
                .getUser().getEmail();
        String subject =
                "Appointment Booked - " +
                        appointment.getAppointmentDate();
        String body =
                EmailTemplates.appointmentBooked(appointment);
        sendEmail(email, subject, body);
        log.info("Appointment booked email sent to: {}",
                email);
    }

    // Appointment confirmed
    @Async
    public void sendAppointmentConfirmedEmail(
            Appointment appointment) {
        String email = appointment.getPatient()
                .getUser().getEmail();
        String subject =
                "Appointment Confirmed - " +
                        appointment.getAppointmentDate();
        String body =
                EmailTemplates.appointmentConfirmed(appointment);
        sendEmail(email, subject, body);
    }

    // Appointment cancelled
    @Async
    public void sendAppointmentCancelledEmail(
            Appointment appointment) {
        String email = appointment.getPatient()
                .getUser().getEmail();
        String subject = "Appointment Cancelled";
        String body =
                EmailTemplates.appointmentCancelled(appointment);
        sendEmail(email, subject, body);
    }

    // Bill generated
    @Async
    public void sendBillGeneratedEmail(Bill bill) {
        String email = bill.getAppointment()
                .getPatient().getUser().getEmail();
        String subject =
                "Bill Generated - ₹" + bill.getTotalAmount();
        String body = EmailTemplates.billGenerated(bill);
        sendEmail(email, subject, body);
    }
}
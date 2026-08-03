package com.hospital.hms.service.email;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Bill;
import com.hospital.hms.model.User;

public class EmailTemplates {

    // ─── Brand config ─────────────────────────────────────
    private static final String HOSPITAL_NAME =
            "AarogyaAI Hospital";
    private static final String PRIMARY   = "#0F9488";
    private static final String SUCCESS   = "#22C55E";
    private static final String WARNING   = "#F59E0B";
    private static final String DANGER    = "#EF4444";
    private static final String INFO      = "#3B82F6";

    // ─── Base layout ──────────────────────────────────────
    private static String base(
            String accentColor,
            String headerIcon,
            String headerTitle,
            String content) {
        return """
            <!DOCTYPE html>
            <html lang="en">
            <head>
              <meta charset="UTF-8"/>
              <meta name="viewport"
                    content="width=device-width,
                             initial-scale=1.0"/>
              <title>%s</title>
            </head>
            <body style="margin:0;padding:0;
                         background-color:#F8FAFC;
                         font-family:-apple-system,
                         BlinkMacSystemFont,'Segoe UI',
                         Roboto,sans-serif;">

              <table width="100%%" cellpadding="0"
                     cellspacing="0"
                     style="background:#F8FAFC;
                            padding:40px 20px;">
                <tr>
                  <td align="center">
                    <table width="600" cellpadding="0"
                           cellspacing="0"
                           style="max-width:600px;
                                  width:100%%;
                                  background:#FFFFFF;
                                  border-radius:16px;
                                  overflow:hidden;
                                  box-shadow:0 4px 6px
                                  rgba(0,0,0,0.05);">

                      <!-- Header -->
                      <tr>
                        <td style="background:%s;
                                   padding:32px 40px;
                                   text-align:center;">
                          <p style="margin:0;
                                    font-size:28px;">
                            %s
                          </p>
                          <h1 style="margin:8px 0 0;
                                     color:#FFFFFF;
                                     font-size:20px;
                                     font-weight:700;
                                     letter-spacing:-0.5px;">
                            %s
                          </h1>
                        </td>
                      </tr>

                      <!-- Content -->
                      <tr>
                        <td style="padding:40px;">
                          %s
                        </td>
                      </tr>

                      <!-- Footer -->
                      <tr>
                        <td style="background:#F8FAFC;
                                   padding:24px 40px;
                                   border-top:1px solid
                                   #E2E8F0;
                                   text-align:center;">
                          <p style="margin:0;
                                    color:#94A3B8;
                                    font-size:12px;
                                    line-height:1.6;">
                            This is an automated message
                            from <strong>%s</strong>.<br/>
                            Please do not reply to
                            this email.
                          </p>
                          <p style="margin:8px 0 0;
                                    color:#CBD5E1;
                                    font-size:11px;">
                            © 2026 %s.
                            All rights reserved.
                          </p>
                        </td>
                      </tr>

                    </table>
                  </td>
                </tr>
              </table>
            </body>
            </html>
            """.formatted(
                headerTitle,
                accentColor,
                headerIcon,
                headerTitle,
                content,
                HOSPITAL_NAME,
                HOSPITAL_NAME
        );
    }

    // ─── Info row helper ──────────────────────────────────
    private static String infoRow(
            String label, String value) {
        return """
            <tr>
              <td style="padding:10px 16px;
                         color:#64748B;
                         font-size:14px;
                         border-bottom:1px solid #F1F5F9;
                         width:40%%;">
                %s
              </td>
              <td style="padding:10px 16px;
                         color:#1E293B;
                         font-size:14px;
                         font-weight:600;
                         border-bottom:1px solid #F1F5F9;">
                %s
              </td>
            </tr>
            """.formatted(label, value);
    }

    // ─── Info table helper ────────────────────────────────
    private static String infoTable(String rows) {
        return """
            <table width="100%%" cellpadding="0"
                   cellspacing="0"
                   style="background:#F8FAFC;
                          border-radius:12px;
                          overflow:hidden;
                          border:1px solid #E2E8F0;
                          margin:20px 0;">
              %s
            </table>
            """.formatted(rows);
    }

    // ─── Alert box helper ─────────────────────────────────
    private static String alertBox(
            String color, String bgColor,
            String icon, String text) {
        return """
            <div style="background:%s;
                        border-left:4px solid %s;
                        border-radius:8px;
                        padding:16px;
                        margin:20px 0;">
              <p style="margin:0;
                        color:%s;
                        font-size:14px;">
                %s %s
              </p>
            </div>
            """.formatted(bgColor, color,
                color, icon, text);
    }

    // ─── CTA button helper ────────────────────────────────
    private static String ctaButton(
            String color, String text, String href) {
        return """
            <div style="text-align:center;
                        margin:28px 0 8px;">
              <a href="%s"
                 style="background:%s;
                        color:#FFFFFF;
                        text-decoration:none;
                        padding:14px 32px;
                        border-radius:10px;
                        font-size:15px;
                        font-weight:600;
                        display:inline-block;">
                %s
              </a>
            </div>
            """.formatted(href, color, text);
    }

    // ══════════════════════════════════════════════════════
    // 1. APPOINTMENT BOOKED
    // ══════════════════════════════════════════════════════
    public static String appointmentBooked(
            Appointment a) {
        String rows =
                infoRow("Doctor",
                        "Dr. " + a.getDoctor()
                                .getUser().getName()) +
                        infoRow("Department",
                                a.getDoctor()
                                        .getDepartment().getName()) +
                        infoRow("Date",
                                a.getAppointmentDate().toString()) +
                        infoRow("Time",
                                a.getAppointmentTime().toString()) +
                        infoRow("Reason",
                                a.getReason() != null
                                        ? a.getReason() : "General checkup") +
                        infoRow("Consultation Fee",
                                "₹" + a.getDoctor()
                                        .getConsultationFee());

        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Appointment Booked! 🎉
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              Your appointment has been successfully
              booked. Here are the details:
            </p>
            %s
            %s
            <p style="color:#64748B;
                      font-size:14px;
                      line-height:1.6;
                      margin-top:24px;">
              Please arrive <strong>15 minutes early</strong>
              and bring any previous medical records
              if available.
            </p>
            """.formatted(
                a.getPatient().getUser().getName(),
                infoTable(rows),
                alertBox(
                        INFO, "#EFF6FF", "ℹ️",
                        "Your appointment status is " +
                                "<strong>PENDING</strong>. " +
                                "You will receive a confirmation " +
                                "once the doctor confirms it."
                )
        );

        return base(PRIMARY, "🏥",
                "Appointment Booked", content);
    }

    // ══════════════════════════════════════════════════════
    // 2. APPOINTMENT CONFIRMED
    // ══════════════════════════════════════════════════════
    public static String appointmentConfirmed(
            Appointment a) {
        String rows =
                infoRow("Doctor",
                        "Dr. " + a.getDoctor()
                                .getUser().getName()) +
                        infoRow("Department",
                                a.getDoctor()
                                        .getDepartment().getName()) +
                        infoRow("Date",
                                a.getAppointmentDate().toString()) +
                        infoRow("Time",
                                a.getAppointmentTime().toString());

        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Appointment Confirmed ✅
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              Great news! Your appointment has been
              confirmed by the doctor.
            </p>
            %s
            %s
            """.formatted(
                a.getPatient().getUser().getName(),
                infoTable(rows),
                alertBox(
                        SUCCESS, "#F0FDF4", "✅",
                        "Please arrive 15 minutes before " +
                                "your scheduled time and bring " +
                                "any previous prescriptions " +
                                "or test reports."
                )
        );

        return base(SUCCESS, "✅",
                "Appointment Confirmed", content);
    }

    // ══════════════════════════════════════════════════════
    // 3. APPOINTMENT CANCELLED
    // ══════════════════════════════════════════════════════
    public static String appointmentCancelled(
            Appointment a) {
        String rows =
                infoRow("Doctor",
                        "Dr. " + a.getDoctor()
                                .getUser().getName()) +
                        infoRow("Date",
                                a.getAppointmentDate().toString()) +
                        infoRow("Time",
                                a.getAppointmentTime().toString());

        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Appointment Cancelled
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              Your appointment has been cancelled.
              Details of the cancelled appointment:
            </p>
            %s
            %s
            <p style="color:#64748B;
                      font-size:14px;
                      line-height:1.6;
                      margin-top:24px;">
              If you would like to reschedule, please
              contact us or book a new appointment
              through the portal.
            </p>
            """.formatted(
                a.getPatient().getUser().getName(),
                infoTable(rows),
                alertBox(
                        DANGER, "#FEF2F2", "❌",
                        "If you did not request this " +
                                "cancellation please contact " +
                                "the hospital immediately."
                )
        );

        return base(DANGER, "❌",
                "Appointment Cancelled", content);
    }

    // ══════════════════════════════════════════════════════
    // 4. BILL GENERATED
    // ══════════════════════════════════════════════════════
    public static String billGenerated(Bill bill) {
        String rows =
                infoRow("Doctor",
                        "Dr. " + bill.getAppointment()
                                .getDoctor().getUser().getName()) +
                        infoRow("Department",
                                bill.getAppointment()
                                        .getDoctor().getDepartment().getName()) +
                        infoRow("Appointment Date",
                                bill.getAppointment()
                                        .getAppointmentDate().toString()) +
                        infoRow("Consultation Fee",
                                "₹" + bill.getConsultationFee()) +
                        infoRow("Additional Charges",
                                "₹" + bill.getAdditionalCharges()) +
                        infoRow("Total Amount",
                                "₹" + bill.getTotalAmount());

        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Bill Generated 🧾
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              A bill has been generated for your
              recent appointment. Here is a summary:
            </p>
            %s

            <!-- Total highlight -->
            <div style="background:#FFFBEB;
                        border:2px solid #F59E0B;
                        border-radius:12px;
                        padding:20px;
                        text-align:center;
                        margin:20px 0;">
              <p style="margin:0;
                        color:#92400E;
                        font-size:14px;">
                Total Amount Due
              </p>
              <p style="margin:8px 0 0;
                        color:#78350F;
                        font-size:32px;
                        font-weight:700;">
                ₹%s
              </p>
            </div>
            %s
            """.formatted(
                bill.getAppointment()
                        .getPatient().getUser().getName(),
                infoTable(rows),
                bill.getTotalAmount(),
                alertBox(
                        WARNING, "#FFFBEB", "💳",
                        "Please visit the reception desk " +
                                "or use the patient portal to " +
                                "complete your payment."
                )
        );

        return base(WARNING, "🧾",
                "Bill Generated", content);
    }

    // ══════════════════════════════════════════════════════
    // 5. PAYMENT RECEIVED ✅ NEW
    // ══════════════════════════════════════════════════════
    public static String paymentReceived(Bill bill) {
        String rows =
                infoRow("Patient",
                        bill.getAppointment()
                                .getPatient().getUser().getName()) +
                        infoRow("Doctor",
                                "Dr. " + bill.getAppointment()
                                        .getDoctor().getUser().getName()) +
                        infoRow("Amount Paid",
                                "₹" + bill.getTotalAmount()) +
                        infoRow("Payment Method",
                                bill.getPaymentMethod() != null
                                        ? bill.getPaymentMethod() : "—") +
                        infoRow("Bill ID", "#" + bill.getId());

        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Payment Received 💚
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              We have received your payment successfully.
              Here is your payment confirmation:
            </p>
            %s

            <!-- Payment success highlight -->
            <div style="background:#F0FDF4;
                        border:2px solid #22C55E;
                        border-radius:12px;
                        padding:20px;
                        text-align:center;
                        margin:20px 0;">
              <p style="margin:0;
                        color:#166534;
                        font-size:14px;">
                Amount Paid
              </p>
              <p style="margin:8px 0 0;
                        color:#15803D;
                        font-size:32px;
                        font-weight:700;">
                ₹%s
              </p>
              <p style="margin:8px 0 0;
                        color:#16A34A;
                        font-size:13px;">
                ✅ Payment Successful
              </p>
            </div>
            %s
            """.formatted(
                bill.getAppointment()
                        .getPatient().getUser().getName(),
                infoTable(rows),
                bill.getTotalAmount(),
                alertBox(
                        SUCCESS, "#F0FDF4", "🧾",
                        "Please keep this email as your " +
                                "payment receipt. Thank you for " +
                                "choosing " + HOSPITAL_NAME + "."
                )
        );

        return base(SUCCESS, "💚",
                "Payment Received", content);
    }

    // ══════════════════════════════════════════════════════
    // 6. ACCOUNT APPROVED ✅ NEW
    // ══════════════════════════════════════════════════════
    public static String accountApproved(User user) {
        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Account Approved! 🎉
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              Great news! Your account has been reviewed
              and approved by our reception team.
              You can now access all patient portal
              features.
            </p>

            <!-- Feature list -->
            <div style="background:#F0FDF4;
                        border-radius:12px;
                        padding:24px;
                        margin:24px 0;">
              <p style="margin:0 0 16px;
                        color:#166534;
                        font-weight:700;
                        font-size:15px;">
                What you can do now:
              </p>
              <p style="margin:8px 0;
                        color:#15803D;
                        font-size:14px;">
                ✅ Book appointments with doctors
              </p>
              <p style="margin:8px 0;
                        color:#15803D;
                        font-size:14px;">
                ✅ View your medical records
              </p>
              <p style="margin:8px 0;
                        color:#15803D;
                        font-size:14px;">
                ✅ Access prescriptions
              </p>
              <p style="margin:8px 0;
                        color:#15803D;
                        font-size:14px;">
                ✅ View and pay bills online
              </p>
            </div>
            %s
            """.formatted(
                user.getName(),
                alertBox(
                        INFO, "#EFF6FF", "🔐",
                        "Login with your registered email " +
                                "and password to get started."
                )
        );

        return base(SUCCESS, "✅",
                "Account Approved", content);
    }

    // ══════════════════════════════════════════════════════
    // 7. ACCOUNT REJECTED ✅ NEW
    // ══════════════════════════════════════════════════════
    public static String accountRejected(User user) {
        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Registration Not Approved
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              We regret to inform you that your
              registration request has not been
              approved at this time.
            </p>
            %s
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;
                      margin-top:20px;">
              If you believe this is an error or
              would like more information, please
              contact our reception desk directly.
            </p>

            <!-- Contact info -->
            <div style="background:#F8FAFC;
                        border-radius:12px;
                        padding:20px;
                        margin:20px 0;
                        text-align:center;">
              <p style="margin:0;
                        color:#64748B;
                        font-size:14px;">
                📞 Contact Reception for assistance
              </p>
            </div>
            """.formatted(
                user.getName(),
                alertBox(
                        DANGER, "#FEF2F2", "❌",
                        "Your account has been deactivated. " +
                                "Please contact the hospital " +
                                "for further assistance."
                )
        );

        return base(DANGER, "❌",
                "Registration Status", content);
    }

    // ══════════════════════════════════════════════════════
    // 8. MEDICAL RECORD ADDED ✅ NEW
    // ══════════════════════════════════════════════════════
    public static String medicalRecordAdded(
            Appointment a,
            String diagnosis) {
        String rows =
                infoRow("Doctor",
                        "Dr. " + a.getDoctor()
                                .getUser().getName()) +
                        infoRow("Department",
                                a.getDoctor()
                                        .getDepartment().getName()) +
                        infoRow("Visit Date",
                                a.getAppointmentDate().toString()) +
                        infoRow("Diagnosis", diagnosis);

        String content = """
            <h2 style="color:#1E293B;
                       margin:0 0 8px;
                       font-size:22px;">
              Medical Record Added 📋
            </h2>
            <p style="color:#64748B;
                      font-size:15px;
                      margin:0 0 24px;">
              Dear <strong>%s</strong>,
            </p>
            <p style="color:#475569;
                      font-size:15px;
                      line-height:1.6;">
              A medical record has been added for
              your recent visit. Here is a summary:
            </p>
            %s
            %s
            """.formatted(
                a.getPatient().getUser().getName(),
                infoTable(rows),
                alertBox(
                        INFO, "#EFF6FF", "📱",
                        "You can view your complete medical " +
                                "record and prescriptions in the " +
                                "patient portal."
                )
        );

        return base(PRIMARY, "📋",
                "Medical Record Added", content);
    }
}

package com.hospital.hms.service.email;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Bill;

public class EmailTemplates {

    // Appointment booked email
    public static String appointmentBooked(
            Appointment appointment) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;
                         max-width: 600px; margin: auto;
                         padding: 20px;">
            
                <div style="background-color: #2196F3;
                            padding: 20px;
                            border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">
                        🏥 Hospital Management System
                    </h1>
                </div>
            
                <div style="background: #f9f9f9;
                            padding: 30px;
                            border-radius: 0 0 8px 8px;
                            border: 1px solid #ddd;">
            
                    <h2 style="color: #333;">
                        Appointment Confirmed! ✅
                    </h2>
            
                    <p>Dear <strong>%s</strong>,</p>
            
                    <p>Your appointment has been successfully
                       booked. Here are the details:</p>
            
                    <div style="background: white;
                                padding: 20px;
                                border-radius: 8px;
                                border-left: 4px solid #2196F3;
                                margin: 20px 0;">
                        <table style="width:100%%">
                            <tr>
                                <td><strong>Doctor:</strong></td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td><strong>Department:</strong></td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td><strong>Date:</strong></td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td><strong>Time:</strong></td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td><strong>Reason:</strong></td>
                                <td>%s</td>
                            </tr>
                            <tr>
                                <td><strong>Fee:</strong></td>
                                <td>₹%s</td>
                            </tr>
                        </table>
                    </div>
            
                    <p style="color: #666;">
                        Please arrive 15 minutes before
                        your scheduled time.
                    </p>
            
                    <p style="color: #666;">
                        Thank you for choosing our hospital.
                    </p>
            
                    <hr style="border: 1px solid #eee;">
                    <p style="color: #999; font-size: 12px;">
                        This is an automated message.
                        Please do not reply to this email.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                appointment.getPatient().getUser().getName(),
                appointment.getDoctor().getUser().getName(),
                appointment.getDoctor().getDepartment().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime(),
                appointment.getReason(),
                appointment.getDoctor().getConsultationFee()
        );
    }

    // Appointment confirmed by doctor
    public static String appointmentConfirmed(
            Appointment appointment) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;
                         max-width: 600px; margin: auto;
                         padding: 20px;">
                <div style="background-color: #4CAF50;
                            padding: 20px;
                            border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">
                        🏥 Hospital Management System
                    </h1>
                </div>
                <div style="background: #f9f9f9;
                            padding: 30px;
                            border-radius: 0 0 8px 8px;
                            border: 1px solid #ddd;">
                    <h2 style="color: #4CAF50;">
                        Appointment Confirmed by Doctor ✅
                    </h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your appointment with
                       <strong>%s</strong> on
                       <strong>%s at %s</strong>
                       has been confirmed.</p>
                    <p style="color: #666;">
                        Please arrive 15 minutes early.
                        Bring any previous medical records.
                    </p>
                </div>
            </body>
            </html>
            """.formatted(
                appointment.getPatient().getUser().getName(),
                appointment.getDoctor().getUser().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );
    }

    // Appointment cancelled
    public static String appointmentCancelled(
            Appointment appointment) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;
                         max-width: 600px; margin: auto;
                         padding: 20px;">
                <div style="background-color: #f44336;
                            padding: 20px;
                            border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">
                        🏥 Hospital Management System
                    </h1>
                </div>
                <div style="background: #f9f9f9;
                            padding: 30px;
                            border-radius: 0 0 8px 8px;
                            border: 1px solid #ddd;">
                    <h2 style="color: #f44336;">
                        Appointment Cancelled ❌
                    </h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your appointment with
                       <strong>%s</strong> scheduled for
                       <strong>%s at %s</strong>
                       has been cancelled.</p>
                    <p>Please contact us to reschedule.</p>
                </div>
            </body>
            </html>
            """.formatted(
                appointment.getPatient().getUser().getName(),
                appointment.getDoctor().getUser().getName(),
                appointment.getAppointmentDate(),
                appointment.getAppointmentTime()
        );
    }

    // Bill generated
    public static String billGenerated(Bill bill) {
        return """
            <html>
            <body style="font-family: Arial, sans-serif;
                         max-width: 600px; margin: auto;
                         padding: 20px;">
                <div style="background-color: #FF9800;
                            padding: 20px;
                            border-radius: 8px 8px 0 0;">
                    <h1 style="color: white; margin: 0;">
                        🏥 Hospital Management System
                    </h1>
                </div>
                <div style="background: #f9f9f9;
                            padding: 30px;
                            border-radius: 0 0 8px 8px;
                            border: 1px solid #ddd;">
                    <h2 style="color: #FF9800;">
                        Bill Generated 💰
                    </h2>
                    <p>Dear <strong>%s</strong>,</p>
                    <p>Your bill has been generated
                       for your appointment:</p>
                    <div style="background: white;
                                padding: 20px;
                                border-radius: 8px;
                                border-left: 4px solid
                                             #FF9800;">
                        <table style="width:100%%">
                            <tr>
                                <td>Consultation Fee:</td>
                                <td>₹%s</td>
                            </tr>
                            <tr>
                                <td>Additional Charges:</td>
                                <td>₹%s</td>
                            </tr>
                            <tr style="font-weight:bold">
                                <td>Total Amount:</td>
                                <td>₹%s</td>
                            </tr>
                        </table>
                    </div>
                    <p>Please visit the reception
                       to make payment.</p>
                </div>
            </body>
            </html>
            """.formatted(
                bill.getAppointment()
                        .getPatient().getUser().getName(),
                bill.getConsultationFee(),
                bill.getAdditionalCharges(),
                bill.getTotalAmount()
        );
    }
}
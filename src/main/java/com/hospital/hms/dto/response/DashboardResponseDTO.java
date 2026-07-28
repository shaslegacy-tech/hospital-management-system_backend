package com.hospital.hms.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponseDTO {
    // Users
    private Long totalDoctors;
    private Long totalPatients;

    // Appointments
    private Long totalAppointments;
    private Long pendingAppointments;
    private Long confirmedAppointments;
    private Long completedAppointments;
    private Long cancelledAppointments;
    private Long todaysAppointments;

    // Revenue
    private Double totalRevenue;
    private Long totalPaidBills;
    private Long totalPendingBills;

    // Departments
    private Long totalDepartments;
}
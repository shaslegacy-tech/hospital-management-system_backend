package com.hospital.hms.service;

import com.hospital.hms.dto.response.DashboardResponseDTO;
import com.hospital.hms.model.enums.*;
import com.hospital.hms.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;

@Slf4j
@Service
public class DashboardService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private BillRepository billRepository;

    @Autowired
    private DepartmentRepository departmentRepository;

    public DashboardResponseDTO getDashboardStats() {
        log.info("Fetching dashboard statistics");

        // User counts
        Long totalDoctors = (long) doctorRepository
                .findAll().size();
        Long totalPatients = (long) patientRepository
                .findAll().size();

        // Appointment counts
        Long totalAppointments = appointmentRepository
                .count();
        Long pending = appointmentRepository
                .countByStatus(AppointmentStatus.PENDING);
        Long confirmed = appointmentRepository
                .countByStatus(AppointmentStatus.CONFIRMED);
        Long completed = appointmentRepository
                .countByStatus(AppointmentStatus.COMPLETED);
        Long cancelled = appointmentRepository
                .countByStatus(AppointmentStatus.CANCELLED);
        Long todaysAppointments = appointmentRepository
                .countByAppointmentDate(LocalDate.now());

        // Revenue
        Double totalRevenue = billRepository
                .getTotalRevenue();
        Long paidBills = billRepository
                .countByStatus(BillStatus.PAID);
        Long pendingBills = billRepository
                .countByStatus(BillStatus.PENDING);

        // Departments
        Long totalDepartments = departmentRepository
                .count();

        return new DashboardResponseDTO(
                totalDoctors,
                totalPatients,
                totalAppointments,
                pending,
                confirmed,
                completed,
                cancelled,
                todaysAppointments,
                totalRevenue != null ? totalRevenue : 0.0,
                paidBills,
                pendingBills,
                totalDepartments
        );
    }
}
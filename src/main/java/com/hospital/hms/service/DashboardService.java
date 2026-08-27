package com.hospital.hms.service;

import com.hospital.hms.dto.response.DashboardResponseDTO;
import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.Bill;
import com.hospital.hms.model.Doctor;
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.enums.AppointmentStatus;
import com.hospital.hms.model.enums.BillStatus;
import com.hospital.hms.repository.AppointmentRepository;
import com.hospital.hms.repository.BillRepository;
import com.hospital.hms.repository.DepartmentRepository;
import com.hospital.hms.repository.DoctorRepository;
import com.hospital.hms.repository.PatientRepository;

import lombok.extern.slf4j.Slf4j;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Slf4j
@Service
public class DashboardService {

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

    @Autowired
    private HospitalContextService hospitalContextService;


    public DashboardResponseDTO getDashboardStats(
            Authentication authentication) {

        log.info(
                "Fetching dashboard statistics for user: {}",
                authentication.getName()
        );

        if (hospitalContextService.isSuperAdmin(authentication)) {

            log.info(
                    "User {} is SUPER_ADMIN. Returning global dashboard.",
                    authentication.getName()
            );

            return getGlobalDashboardStats();
        }


        Hospital hospital =
                hospitalContextService
                        .getCurrentUserHospital(authentication);

        Long hospitalId =
                hospital.getId();

        log.info(
                "Building dashboard for hospitalId: {}",
                hospitalId
        );

        List<Doctor> hospitalDoctors =
                doctorRepository.findAll()
                        .stream()
                        .filter(this::hasValidHospital)
                        .filter(doctor ->
                                doctor.getDepartment()
                                        .getHospital()
                                        .getId()
                                        .equals(hospitalId)
                        )
                        .toList();

        long totalDoctors =
                hospitalDoctors.size();


        List<Appointment> hospitalAppointments =
                appointmentRepository.findAll()
                        .stream()
                        .filter(this::hasValidDoctorHospital)
                        .filter(appointment ->
                                appointment
                                        .getDoctor()
                                        .getDepartment()
                                        .getHospital()
                                        .getId()
                                        .equals(hospitalId)
                        )
                        .toList();


        long totalAppointments =
                hospitalAppointments.size();


        long pending =
                hospitalAppointments.stream()
                        .filter(appointment ->
                                appointment.getStatus()
                                        == AppointmentStatus.PENDING
                        )
                        .count();


        long confirmed =
                hospitalAppointments.stream()
                        .filter(appointment ->
                                appointment.getStatus()
                                        == AppointmentStatus.CONFIRMED
                        )
                        .count();


        long completed =
                hospitalAppointments.stream()
                        .filter(appointment ->
                                appointment.getStatus()
                                        == AppointmentStatus.COMPLETED
                        )
                        .count();


        long cancelled =
                hospitalAppointments.stream()
                        .filter(appointment ->
                                appointment.getStatus()
                                        == AppointmentStatus.CANCELLED
                        )
                        .count();

        long todaysAppointments =
                hospitalAppointments.stream()
                        .filter(appointment ->
                                appointment.getAppointmentDate() != null
                                        && appointment
                                                .getAppointmentDate()
                                                .equals(LocalDate.now())
                        )
                        .count();


        long totalPatients =
                hospitalAppointments.stream()
                        .filter(appointment ->
                                appointment.getPatient() != null
                        )
                        .map(appointment ->
                                appointment.getPatient().getId()
                        )
                        .distinct()
                        .count();



        List<Bill> hospitalBills =
                billRepository.findAll()
                        .stream()
                        .filter(this::hasValidBillHospital)
                        .filter(bill ->
                                bill.getAppointment()
                                        .getDoctor()
                                        .getDepartment()
                                        .getHospital()
                                        .getId()
                                        .equals(hospitalId)
                        )
                        .toList();

        double totalRevenue =
                hospitalBills.stream()
                        .filter(bill ->
                                bill.getStatus()
                                        == BillStatus.PAID
                        )
                        .mapToDouble(Bill::getTotalAmount)
                        .sum();

        long paidBills =
                hospitalBills.stream()
                        .filter(bill ->
                                bill.getStatus()
                                        == BillStatus.PAID
                        )
                        .count();

        long pendingBills =
                hospitalBills.stream()
                        .filter(bill ->
                                bill.getStatus()
                                        == BillStatus.PENDING
                        )
                        .count();

        long totalDepartments =
                departmentRepository.findAll()
                        .stream()
                        .filter(department ->
                                department.getHospital() != null
                                        && department
                                                .getHospital()
                                                .getId()
                                                .equals(hospitalId)
                        )
                        .count();

        log.info(
                "Hospital dashboard completed. hospitalId={}, " +
                "doctors={}, patients={}, appointments={}, " +
                "revenue={}, departments={}",
                hospitalId,
                totalDoctors,
                totalPatients,
                totalAppointments,
                totalRevenue,
                totalDepartments
        );

        return new DashboardResponseDTO(
                totalDoctors,
                totalPatients,
                totalAppointments,

                pending,
                confirmed,
                completed,
                cancelled,

                todaysAppointments,

                totalRevenue,

                paidBills,
                pendingBills,

                totalDepartments
        );
    }

    private DashboardResponseDTO getGlobalDashboardStats() {

        log.info(
                "Fetching GLOBAL dashboard statistics"
        );

        long totalDoctors =
                doctorRepository.count();

        long totalAppointments =
                appointmentRepository.count();

        long pending =
                appointmentRepository.countByStatus(
                        AppointmentStatus.PENDING
                );


        long confirmed =
                appointmentRepository.countByStatus(
                        AppointmentStatus.CONFIRMED
                );


        long completed =
                appointmentRepository.countByStatus(
                        AppointmentStatus.COMPLETED
                );


        long cancelled =
                appointmentRepository.countByStatus(
                        AppointmentStatus.CANCELLED
                );

        long todaysAppointments =
                appointmentRepository.countByAppointmentDate(
                        LocalDate.now()
                );


        long totalPatients =
                patientRepository.count();


        Double revenue =
                billRepository.getTotalRevenue();

        double totalRevenue =
                revenue != null
                        ? revenue
                        : 0.0;

        long paidBills =
                billRepository.countByStatus(
                        BillStatus.PAID
                );


        long pendingBills =
                billRepository.countByStatus(
                        BillStatus.PENDING
                );

        long totalDepartments =
                departmentRepository.count();


        log.info(
                "Global dashboard completed. doctors={}, " +
                "patients={}, appointments={}, revenue={}, " +
                "departments={}",
                totalDoctors,
                totalPatients,
                totalAppointments,
                totalRevenue,
                totalDepartments
        );

        return new DashboardResponseDTO(
                totalDoctors,
                totalPatients,
                totalAppointments,

                pending,
                confirmed,
                completed,
                cancelled,

                todaysAppointments,

                totalRevenue,

                paidBills,
                pendingBills,

                totalDepartments
        );
    }


    private boolean hasValidHospital(
            Doctor doctor) {

        return doctor != null
                && doctor.getDepartment() != null
                && doctor.getDepartment().getHospital() != null
                && doctor.getDepartment()
                        .getHospital()
                        .getId() != null;
    }


    private boolean hasValidDoctorHospital(
            Appointment appointment) {

        return appointment != null
                && appointment.getDoctor() != null
                && appointment.getDoctor().getDepartment() != null
                && appointment.getDoctor()
                        .getDepartment()
                        .getHospital() != null
                && appointment.getDoctor()
                        .getDepartment()
                        .getHospital()
                        .getId() != null;
    }

    private boolean hasValidBillHospital(
            Bill bill) {

        return bill != null
                && bill.getAppointment() != null
                && bill.getAppointment().getDoctor() != null
                && bill.getAppointment()
                        .getDoctor()
                        .getDepartment() != null
                && bill.getAppointment()
                        .getDoctor()
                        .getDepartment()
                        .getHospital() != null
                && bill.getAppointment()
                        .getDoctor()
                        .getDepartment()
                        .getHospital()
                        .getId() != null;
    }
}
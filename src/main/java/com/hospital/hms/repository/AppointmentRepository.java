package com.hospital.hms.repository;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.enums.AppointmentStatus;
import org.springframework.data.domain.*;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface AppointmentRepository
        extends JpaRepository<Appointment, Long>,
        JpaSpecificationExecutor<Appointment> {

    // Find by patient
    Page<Appointment> findByPatientId(
            Long patientId, Pageable pageable);

    // Find by doctor
    Page<Appointment> findByDoctorId(
            Long doctorId, Pageable pageable);

    // Find by doctor and date
    List<Appointment> findByDoctorIdAndAppointmentDate(
            Long doctorId, LocalDate date);

    // Find by status
    Page<Appointment> findByStatus(
            AppointmentStatus status, Pageable pageable);

    // Today's appointments
    List<Appointment> findByAppointmentDate(LocalDate date);

    // Check slot availability
    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusNot(
            Long doctorId,
            LocalDate date,
            java.time.LocalTime time,
            AppointmentStatus status);

    // Count by status for dashboard
    Long countByStatus(AppointmentStatus status);

    // Count today's appointments
    Long countByAppointmentDate(LocalDate date);

    boolean existsByDoctorIdAndPatientId(
        Long doctorId,
        Long patientId
);
}
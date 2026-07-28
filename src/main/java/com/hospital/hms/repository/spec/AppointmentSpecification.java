package com.hospital.hms.repository.spec;

import com.hospital.hms.model.Appointment;
import com.hospital.hms.model.enums.AppointmentStatus;
import org.springframework.data.jpa.domain.Specification;

import java.time.LocalDate;

public class AppointmentSpecification {

    public static Specification<Appointment> hasStatus(
            AppointmentStatus status) {
        return (root, query, cb) -> {
            if (status == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("status"), status);
        };
    }

    public static Specification<Appointment>
    hasDoctorId(Long doctorId) {
        return (root, query, cb) -> {
            if (doctorId == null) {
                return cb.conjunction();
            }
            return cb.equal(
                    root.get("doctor").get("id"), doctorId);
        };
    }

    public static Specification<Appointment>
    hasPatientId(Long patientId) {
        return (root, query, cb) -> {
            if (patientId == null) {
                return cb.conjunction();
            }
            return cb.equal(
                    root.get("patient").get("id"), patientId);
        };
    }

    public static Specification<Appointment>
    hasDepartmentId(Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) {
                return cb.conjunction();
            }
            return cb.equal(
                    root.get("doctor").get("department")
                            .get("id"), departmentId);
        };
    }

    public static Specification<Appointment>
    dateFrom(LocalDate from) {
        return (root, query, cb) -> {
            if (from == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(
                    root.get("appointmentDate"), from);
        };
    }

    public static Specification<Appointment>
    dateTo(LocalDate to) {
        return (root, query, cb) -> {
            if (to == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(
                    root.get("appointmentDate"), to);
        };
    }
}
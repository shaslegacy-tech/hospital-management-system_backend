package com.hospital.hms.repository.spec;

import com.hospital.hms.model.Doctor;
import org.springframework.data.jpa.domain.Specification;

public class DoctorSpecification {

    public static Specification<Doctor> hasName(
            String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction(); // always true
            }
            return cb.like(
                    cb.lower(root.get("user").get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Doctor> hasSpecialization(
            String specialization) {
        return (root, query, cb) -> {
            if (specialization == null ||
                    specialization.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("specialization")),
                    "%" + specialization.toLowerCase() + "%");
        };
    }

    public static Specification<Doctor> hasDepartment(
            Long departmentId) {
        return (root, query, cb) -> {
            if (departmentId == null) {
                return cb.conjunction();
            }
            return cb.equal(
                    root.get("department").get("id"),
                    departmentId);
        };
    }

    public static Specification<Doctor> minExperience(
            Integer years) {
        return (root, query, cb) -> {
            if (years == null) {
                return cb.conjunction();
            }
            return cb.greaterThanOrEqualTo(
                    root.get("experienceYears"), years);
        };
    }

    public static Specification<Doctor> isAvailable(
            Boolean available) {
        return (root, query, cb) -> {
            if (available == null) {
                return cb.conjunction();
            }
            return cb.equal(
                    root.get("available"), available);
        };
    }

    public static Specification<Doctor> maxFee(
            Double fee) {
        return (root, query, cb) -> {
            if (fee == null) {
                return cb.conjunction();
            }
            return cb.lessThanOrEqualTo(
                    root.get("consultationFee"), fee);
        };
    }

    public static Specification<Doctor> hasGender(String gender) {
       return (root, query, cb) ->
           gender == null || gender.isBlank()
               ? cb.conjunction()
               : cb.equal(root.get("gender"), gender);
   }
}
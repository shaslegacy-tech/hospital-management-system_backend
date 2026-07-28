package com.hospital.hms.repository.spec;

import com.hospital.hms.model.Patient;
import com.hospital.hms.model.enums.BloodGroup;
import org.springframework.data.jpa.domain.Specification;

public class PatientSpecification {

    public static Specification<Patient> hasName(
            String name) {
        return (root, query, cb) -> {
            if (name == null || name.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("user").get("name")),
                    "%" + name.toLowerCase() + "%");
        };
    }

    public static Specification<Patient> hasBloodGroup(
            BloodGroup group) {
        return (root, query, cb) -> {
            if (group == null) {
                return cb.conjunction();
            }
            return cb.equal(root.get("bloodGroup"), group);
        };
    }

    public static Specification<Patient> hasEmail(
            String email) {
        return (root, query, cb) -> {
            if (email == null || email.isBlank()) {
                return cb.conjunction();
            }
            return cb.like(
                    cb.lower(root.get("user").get("email")),
                    "%" + email.toLowerCase() + "%");
        };
    }
}
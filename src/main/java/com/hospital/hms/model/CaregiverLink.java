package com.hospital.hms.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(
        name = "caregiver_links",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_caregiver_patient",
                        columnNames = {
                                "caregiver_user_id",
                                "patient_id"
                        }
               )
        }
)
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class CaregiverLink extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * User who gets access to the patient's data.
     * Example: adult child, spouse, parent, etc.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "caregiver_user_id",
            nullable = false
    )
    private User caregiver;

    /**
     * Patient whose data the caregiver can access.
     */
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(
            name = "patient_id",
            nullable = false
    )
    private Patient patient;

    /**
     * Relationship between caregiver and patient.
     * Example:
     * Son, Daughter, Spouse, Parent
     */
    @Column(nullable = false)
    private String relationship;

    /**
     * true  = caregiver currently has access
     * false = access revoked
     */
    @Column(nullable = false)
    private boolean active = true;
}
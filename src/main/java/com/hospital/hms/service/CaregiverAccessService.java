package com.hospital.hms.service;

import com.hospital.hms.repository.CaregiverLinkRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CaregiverAccessService {

    private final CaregiverLinkRepository caregiverLinkRepository;

    public boolean canAccess(
            Long requestingUserId,
            Long patientUserId,
            Long patientId
    ) {

        if (requestingUserId.equals(patientUserId)) {
            return true;
        }

        return caregiverLinkRepository
                .existsByCaregiverIdAndPatientIdAndActiveTrue(
                        requestingUserId,
                        patientId
                );
    }

    public void validateAccess(
            Long requestingUserId,
            Long patientUserId,
            Long patientId
    ) {

        if (!canAccess(
                requestingUserId,
                patientUserId,
                patientId
        )) {
            throw new AccessDeniedException(
                    "You do not have access to this patient's data."
            );
        }
    }
}
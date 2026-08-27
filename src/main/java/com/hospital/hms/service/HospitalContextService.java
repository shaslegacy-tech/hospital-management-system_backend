package com.hospital.hms.service;
 
import com.hospital.hms.model.Hospital;
import com.hospital.hms.model.User;
import com.hospital.hms.model.enums.HospitalStatus;
import com.hospital.hms.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Service;
 
@Service
public class HospitalContextService {
 
    @Autowired
    private UserRepository userRepository;
 
    
    // public Hospital getCurrentUserHospital(Authentication authentication) {
    //     User user = userRepository.findByEmail(authentication.getName())
    //         .orElseThrow(() -> new RuntimeException("User not found!"));
    //     if (user.getHospital() == null) {
    //         throw new RuntimeException("Your account isn't linked to a hospital!");
    //     }
    //     return user.getHospital();
    // }

     
    public Hospital getCurrentUserHospital(Authentication authentication) {
        User user = userRepository.findByEmail(authentication.getName())
            .orElseThrow(() -> new RuntimeException("User not found!"));
 
        if (user.getHospital() == null) {
            throw new RuntimeException("Your account isn't linked to a hospital!");
        }
 
        Hospital hospital = user.getHospital();
 
        if (hospital.getStatus() == HospitalStatus.SUSPENDED) {
            throw new RuntimeException(
                "Your hospital account has been suspended. Contact support.");
        }
        if (hospital.getStatus() == HospitalStatus.REJECTED) {
            throw new RuntimeException(
                "Your hospital's registration was not approved.");
        }
 
        return hospital;
    }

    public boolean isSuperAdmin(Authentication authentication) {
        return authentication.getAuthorities().stream()
            .anyMatch(a -> a.getAuthority().equals("ROLE_SUPER_ADMIN"));
    }
}
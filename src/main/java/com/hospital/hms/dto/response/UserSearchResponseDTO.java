package com.hospital.hms.dto.response;

import com.hospital.hms.model.enums.Role;
import com.hospital.hms.model.enums.UserStatus;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserSearchResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
    private UserStatus status;
    private boolean hasPatientRecord;
}

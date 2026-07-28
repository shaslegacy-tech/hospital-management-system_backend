package com.hospital.hms.dto.response;

import com.hospital.hms.model.enums.Role;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AuthResponseDTO {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private Role role;
}
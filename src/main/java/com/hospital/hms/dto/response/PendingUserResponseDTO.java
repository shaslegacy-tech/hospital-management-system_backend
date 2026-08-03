package com.hospital.hms.dto.response;

import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PendingUserResponseDTO {
    private Long userId;
    private String name;
    private String email;
    private String phone;
    private String registeredAt;
}

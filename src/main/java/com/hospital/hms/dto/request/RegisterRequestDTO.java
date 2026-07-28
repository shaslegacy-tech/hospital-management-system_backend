package com.hospital.hms.dto.request;

import com.hospital.hms.model.enums.Role;
import jakarta.validation.constraints.*;
import lombok.*;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RegisterRequestDTO {

    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Email is required")
    @Email(message = "Invalid email format")
    private String email;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password min 6 characters")
    private String password;

    @NotBlank(message = "Phone is required")
    private String phone;

    @NotNull(message = "Role is required")
    private Role role;
}
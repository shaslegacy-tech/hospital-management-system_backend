package com.hospital.hms.dto.response;

import lombok.*;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DepartmentResponseDTO {
    private Long id;
    private String name;
    private String description;
    private boolean active;
    private LocalDateTime createdAt;
}
package com.clinic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private Long departmentId;
    private String departmentName;
    private String specialization;
    private String phone;
    private Integer experienceYears;
    private boolean available;
    private LocalDateTime createdAt;
}

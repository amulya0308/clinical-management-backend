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
public class PatientResponse {
    private Long id;
    private Long userId;
    private String email;
    private String fullName;
    private Integer age;
    private String gender;
    private String phone;
    private String address;
    private LocalDateTime createdAt;
}

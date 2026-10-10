package com.clinic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationResponse {
    private Long id;
    private Long appointmentId;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private String departmentName;
    private Long patientId;
    private String patientName;
    private Integer patientAge;
    private String patientGender;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String diagnosis;
    private String prescription;
    private String notes;
    private LocalDateTime createdAt;
}

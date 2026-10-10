package com.clinic.dto.response;

import com.clinic.entity.AppointmentStatus;
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
public class AppointmentResponse {
    private Long id;
    private Long patientId;
    private String patientName;
    private String patientPhone;
    private Long doctorId;
    private String doctorName;
    private String doctorSpecialization;
    private Long departmentId;
    private String departmentName;
    private LocalDate appointmentDate;
    private LocalTime appointmentTime;
    private String reason;
    private AppointmentStatus status;
    private boolean hasConsultation;
    private Long consultationId;
    private LocalDateTime createdAt;
}

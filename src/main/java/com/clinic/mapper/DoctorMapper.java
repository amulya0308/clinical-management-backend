package com.clinic.mapper;

import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.Doctor;
import org.springframework.stereotype.Component;

@Component
public class DoctorMapper {

    public DoctorResponse toResponse(Doctor doctor) {
        if (doctor == null) return null;
        return DoctorResponse.builder()
                .id(doctor.getId())
                .userId(doctor.getUser() != null ? doctor.getUser().getId() : null)
                .email(doctor.getUser() != null ? doctor.getUser().getEmail() : null)
                .fullName(doctor.getFullName())
                .departmentId(doctor.getDepartment() != null ? doctor.getDepartment().getId() : null)
                .departmentName(doctor.getDepartment() != null ? doctor.getDepartment().getName() : null)
                .specialization(doctor.getSpecialization())
                .phone(doctor.getPhone())
                .experienceYears(doctor.getExperienceYears())
                .available(doctor.isAvailable())
                .createdAt(doctor.getCreatedAt())
                .build();
    }
}

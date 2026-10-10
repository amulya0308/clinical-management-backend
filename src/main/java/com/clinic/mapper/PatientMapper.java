package com.clinic.mapper;

import com.clinic.dto.response.PatientResponse;
import com.clinic.entity.Patient;
import org.springframework.stereotype.Component;

@Component
public class PatientMapper {

    public PatientResponse toResponse(Patient patient) {
        if (patient == null) return null;
        return PatientResponse.builder()
                .id(patient.getId())
                .userId(patient.getUser() != null ? patient.getUser().getId() : null)
                .email(patient.getUser() != null ? patient.getUser().getEmail() : null)
                .fullName(patient.getFullName())
                .age(patient.getAge())
                .gender(patient.getGender())
                .phone(patient.getPhone())
                .address(patient.getAddress())
                .createdAt(patient.getCreatedAt())
                .build();
    }
}

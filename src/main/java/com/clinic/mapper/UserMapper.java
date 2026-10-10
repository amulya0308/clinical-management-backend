package com.clinic.mapper;

import com.clinic.dto.response.UserSummaryResponse;
import com.clinic.entity.Doctor;
import com.clinic.entity.Patient;
import com.clinic.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserSummaryResponse toResponse(User user, Doctor doctor, Patient patient) {
        if (user == null) return null;
        String fullName = null;
        Long profileId = null;

        if (doctor != null) {
            fullName = doctor.getFullName();
            profileId = doctor.getId();
        } else if (patient != null) {
            fullName = patient.getFullName();
            profileId = patient.getId();
        }

        return UserSummaryResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .enabled(user.isEnabled())
                .fullName(fullName)
                .profileId(profileId)
                .createdAt(user.getCreatedAt())
                .build();
    }
}

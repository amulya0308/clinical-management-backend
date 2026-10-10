package com.clinic.mapper;

import com.clinic.dto.response.DepartmentResponse;
import com.clinic.entity.Department;
import org.springframework.stereotype.Component;

@Component
public class DepartmentMapper {

    public DepartmentResponse toResponse(Department department, long doctorCount) {
        if (department == null) return null;
        return DepartmentResponse.builder()
                .id(department.getId())
                .name(department.getName())
                .description(department.getDescription())
                .doctorCount(doctorCount)
                .createdAt(department.getCreatedAt())
                .build();
    }
}

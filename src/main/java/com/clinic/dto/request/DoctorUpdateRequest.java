package com.clinic.dto.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DoctorUpdateRequest {

    @NotBlank(message = "Doctor full name is required")
    private String fullName;

    @NotNull(message = "Department ID is required")
    private Long departmentId;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    @NotBlank(message = "Phone number is required")
    private String phone;

    @NotNull(message = "Experience years is required")
    @Min(value = 0, message = "Experience years cannot be negative")
    private Integer experienceYears;

    @NotNull(message = "Availability status is required")
    private Boolean available;
}

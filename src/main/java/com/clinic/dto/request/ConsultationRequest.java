package com.clinic.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ConsultationRequest {

    @NotBlank(message = "Diagnosis is required")
    private String diagnosis;

    @NotBlank(message = "Prescription is required")
    private String prescription;

    private String notes;
}

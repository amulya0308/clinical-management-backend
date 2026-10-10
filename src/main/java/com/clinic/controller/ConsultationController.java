package com.clinic.controller;

import com.clinic.dto.ApiResponse;
import com.clinic.dto.request.ConsultationRequest;
import com.clinic.dto.response.ConsultationResponse;
import com.clinic.service.ConsultationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "Consultations", description = "Medical consultations, diagnoses, and prescriptions")
public class ConsultationController {

    private final ConsultationService consultationService;

    public ConsultationController(ConsultationService consultationService) {
        this.consultationService = consultationService;
    }

    @PostMapping("/api/doctor/appointments/{appointmentId}/consultation")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Create consultation and mark appointment completed (Doctor)")
    public ResponseEntity<ApiResponse<ConsultationResponse>> createConsultation(
            @PathVariable Long appointmentId,
            @Valid @RequestBody ConsultationRequest request) {
        log.info("Doctor creating consultation for appointment id: {}", appointmentId);
        ConsultationResponse response = consultationService.createConsultation(appointmentId, request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Consultation record saved successfully"));
    }

    @PutMapping("/api/doctor/consultations/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update consultation details (Doctor)")
    public ResponseEntity<ApiResponse<ConsultationResponse>> updateConsultation(
            @PathVariable Long id,
            @Valid @RequestBody ConsultationRequest request) {
        log.info("Doctor updating consultation id: {}", id);
        ConsultationResponse response = consultationService.updateConsultation(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Consultation updated successfully"));
    }

    @GetMapping("/api/appointments/{appointmentId}/consultation")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get consultation for appointment (Patient, Doctor, Admin)")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultationByAppointmentId(
            @PathVariable Long appointmentId) {
        ConsultationResponse response = consultationService.getConsultationByAppointmentId(appointmentId);
        return ResponseEntity.ok(ApiResponse.success(response, "Consultation retrieved successfully"));
    }

    @GetMapping("/api/consultations/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get consultation by ID (Patient, Doctor, Admin)")
    public ResponseEntity<ApiResponse<ConsultationResponse>> getConsultationById(
            @PathVariable Long id) {
        ConsultationResponse response = consultationService.getConsultationById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Consultation retrieved successfully"));
    }
}

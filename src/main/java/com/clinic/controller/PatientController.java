package com.clinic.controller;

import com.clinic.dto.ApiResponse;
import com.clinic.dto.PageResponse;
import com.clinic.dto.request.PatientUpdateRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.service.PatientService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@Tag(name = "Patients", description = "Patient profiles and management")
public class PatientController {

    private final PatientService patientService;

    public PatientController(PatientService patientService) {
        this.patientService = patientService;
    }

    @GetMapping("/api/patient/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get logged-in patient's profile")
    public ResponseEntity<ApiResponse<PatientResponse>> getCurrentPatientProfile() {
        PatientResponse response = patientService.getCurrentPatientProfile();
        return ResponseEntity.ok(ApiResponse.success(response, "Profile retrieved successfully"));
    }

    @PutMapping("/api/patient/profile")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Update logged-in patient's profile")
    public ResponseEntity<ApiResponse<PatientResponse>> updateCurrentPatientProfile(
            @Valid @RequestBody PatientUpdateRequest request) {
        PatientResponse response = patientService.updateCurrentPatientProfile(request);
        return ResponseEntity.ok(ApiResponse.success(response, "Profile updated successfully"));
    }

    @GetMapping("/api/doctor/patients/{id}")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "View patient details (Doctor)")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientByDoctor(@PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient details retrieved successfully"));
    }

    @GetMapping("/api/admin/patients")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all patients with pagination (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<PatientResponse>>> getPatients(
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PageResponse<PatientResponse> response = patientService.getPatients(keyword, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Patients list retrieved successfully"));
    }

    @GetMapping("/api/admin/patients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get patient by ID (Admin)")
    public ResponseEntity<ApiResponse<PatientResponse>> getPatientById(@PathVariable Long id) {
        PatientResponse response = patientService.getPatientById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient retrieved successfully"));
    }

    @PutMapping("/api/admin/patients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update patient by ID (Admin)")
    public ResponseEntity<ApiResponse<PatientResponse>> updatePatient(
            @PathVariable Long id, @Valid @RequestBody PatientUpdateRequest request) {
        PatientResponse response = patientService.updatePatient(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Patient updated successfully"));
    }

    @DeleteMapping("/api/admin/patients/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete patient safely (Admin)")
    public ResponseEntity<Void> deletePatient(@PathVariable Long id) {
        patientService.deletePatient(id);
        return ResponseEntity.noContent().build();
    }
}

package com.clinic.controller;

import com.clinic.dto.ApiResponse;
import com.clinic.dto.response.AdminDashboardResponse;
import com.clinic.dto.response.DoctorDashboardResponse;
import com.clinic.dto.response.PatientDashboardResponse;
import com.clinic.service.DashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Slf4j
@RestController
@RequestMapping("/api")
@Tag(name = "Dashboard", description = "Live statistics and role-specific dashboard views")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @GetMapping("/admin/dashboard")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Get admin dashboard statistics and counts")
    public ResponseEntity<ApiResponse<AdminDashboardResponse>> getAdminDashboard() {
        AdminDashboardResponse response = dashboardService.getAdminDashboard();
        return ResponseEntity.ok(ApiResponse.success(response, "Admin dashboard statistics retrieved"));
    }

    @GetMapping("/doctor/dashboard")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get doctor dashboard schedule and metrics")
    public ResponseEntity<ApiResponse<DoctorDashboardResponse>> getDoctorDashboard() {
        DoctorDashboardResponse response = dashboardService.getDoctorDashboard();
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor dashboard metrics retrieved"));
    }

    @GetMapping("/patient/dashboard")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get patient dashboard overview")
    public ResponseEntity<ApiResponse<PatientDashboardResponse>> getPatientDashboard() {
        PatientDashboardResponse response = dashboardService.getPatientDashboard();
        return ResponseEntity.ok(ApiResponse.success(response, "Patient dashboard overview retrieved"));
    }
}

package com.clinic.controller;

import com.clinic.dto.ApiResponse;
import com.clinic.dto.PageResponse;
import com.clinic.dto.request.AppointmentCreateRequest;
import com.clinic.dto.request.AppointmentStatusUpdateRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.AppointmentStatus;
import com.clinic.service.AppointmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

@Slf4j
@RestController
@Tag(name = "Appointments", description = "Appointment booking, scheduling and lifecycle endpoints")
public class AppointmentController {

    private final AppointmentService appointmentService;

    public AppointmentController(AppointmentService appointmentService) {
        this.appointmentService = appointmentService;
    }

    @PostMapping("/api/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Book a new appointment (Patient)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> bookAppointment(
            @Valid @RequestBody AppointmentCreateRequest request) {
        log.info("Booking appointment for doctor id: {} on date: {}", request.getDoctorId(), request.getAppointmentDate());
        AppointmentResponse response = appointmentService.bookAppointment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Appointment booked successfully"));
    }

    @GetMapping("/api/patient/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Get list of my booked appointments (Patient)")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getPatientAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PageResponse<AppointmentResponse> response = appointmentService.getPatientAppointments(status, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Appointments retrieved successfully"));
    }

    @PutMapping("/api/patient/appointments/{id}/cancel")
    @PreAuthorize("hasRole('PATIENT')")
    @Operation(summary = "Cancel appointment (Patient)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> cancelPatientAppointment(@PathVariable Long id) {
        log.info("Cancelling appointment id: {}", id);
        AppointmentResponse response = appointmentService.cancelPatientAppointment(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Appointment cancelled successfully"));
    }

    @GetMapping("/api/doctor/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Get list of assigned appointments (Doctor)")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getDoctorAppointments(
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PageResponse<AppointmentResponse> response = appointmentService.getDoctorAppointments(status, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor appointments retrieved successfully"));
    }

    @PutMapping("/api/doctor/appointments/{id}/status")
    @PreAuthorize("hasRole('DOCTOR')")
    @Operation(summary = "Update appointment status (Doctor)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> updateDoctorAppointmentStatus(
            @PathVariable Long id, @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        log.info("Doctor updating status of appointment id: {} to {}", id, request.getStatus());
        AppointmentResponse response = appointmentService.updateDoctorAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Appointment status updated successfully"));
    }

    @GetMapping("/api/admin/appointments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "List all clinic appointments with filters (Admin)")
    public ResponseEntity<ApiResponse<PageResponse<AppointmentResponse>>> getAllAppointments(
            @RequestParam(required = false) Long doctorId,
            @RequestParam(required = false) AppointmentStatus status,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "appointmentDate") String sort,
            @RequestParam(defaultValue = "desc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PageResponse<AppointmentResponse> response = appointmentService.getAllAppointments(doctorId, status, date, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "All appointments retrieved successfully"));
    }

    @PutMapping("/api/admin/appointments/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update appointment status (Admin)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> adminUpdateAppointmentStatus(
            @PathVariable Long id, @Valid @RequestBody AppointmentStatusUpdateRequest request) {
        log.info("Admin updating status of appointment id: {} to {}", id, request.getStatus());
        AppointmentResponse response = appointmentService.adminUpdateAppointmentStatus(id, request.getStatus());
        return ResponseEntity.ok(ApiResponse.success(response, "Appointment status updated successfully"));
    }

    @GetMapping("/api/appointments/{id}")
    @PreAuthorize("isAuthenticated()")
    @Operation(summary = "Get appointment details by ID (Authenticated with ownership check)")
    public ResponseEntity<ApiResponse<AppointmentResponse>> getAppointmentById(@PathVariable Long id) {
        AppointmentResponse response = appointmentService.getAppointmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Appointment details retrieved successfully"));
    }
}

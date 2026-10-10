package com.clinic.controller;

import com.clinic.dto.ApiResponse;
import com.clinic.dto.PageResponse;
import com.clinic.dto.request.DoctorCreateRequest;
import com.clinic.dto.request.DoctorUpdateRequest;
import com.clinic.dto.response.DoctorResponse;
import com.clinic.service.DoctorService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "Doctors", description = "Doctor management and directory endpoints")
public class DoctorController {

    private final DoctorService doctorService;

    public DoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping("/api/doctors")
    @Operation(summary = "Browse and search doctors with filters and pagination")
    public ResponseEntity<ApiResponse<PageResponse<DoctorResponse>>> getDoctors(
            @RequestParam(required = false) Long departmentId,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) Boolean available,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "fullName") String sort,
            @RequestParam(defaultValue = "asc") String direction) {

        Sort sortObj = direction.equalsIgnoreCase("desc") ?
                Sort.by(sort).descending() : Sort.by(sort).ascending();
        Pageable pageable = PageRequest.of(page, size, sortObj);

        PageResponse<DoctorResponse> response = doctorService.getDoctors(
                departmentId, specialization, keyword, available, pageable);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctors retrieved successfully"));
    }

    @GetMapping("/api/doctors/all")
    @Operation(summary = "Get list of all active doctors for selection")
    public ResponseEntity<ApiResponse<List<DoctorResponse>>> getAllDoctorsList() {
        List<DoctorResponse> list = doctorService.getAllDoctorsList();
        return ResponseEntity.ok(ApiResponse.success(list, "Available doctors retrieved successfully"));
    }

    @GetMapping("/api/doctors/{id}")
    @Operation(summary = "Get doctor details by ID")
    public ResponseEntity<ApiResponse<DoctorResponse>> getDoctorById(@PathVariable Long id) {
        DoctorResponse response = doctorService.getDoctorById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor retrieved successfully"));
    }

    @PostMapping("/api/admin/doctors")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create doctor profile and credentials (Admin)")
    public ResponseEntity<ApiResponse<DoctorResponse>> createDoctor(@Valid @RequestBody DoctorCreateRequest request) {
        log.info("Admin creating doctor with email: {}", request.getEmail());
        DoctorResponse response = doctorService.createDoctor(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Doctor created successfully"));
    }

    @PutMapping("/api/admin/doctors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update doctor details (Admin)")
    public ResponseEntity<ApiResponse<DoctorResponse>> updateDoctor(
            @PathVariable Long id, @Valid @RequestBody DoctorUpdateRequest request) {
        log.info("Admin updating doctor id: {}", id);
        DoctorResponse response = doctorService.updateDoctor(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Doctor updated successfully"));
    }

    @DeleteMapping("/api/admin/doctors/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete doctor safely (Admin)")
    public ResponseEntity<Void> deleteDoctor(@PathVariable Long id) {
        log.info("Admin deleting doctor id: {}", id);
        doctorService.deleteDoctor(id);
        return ResponseEntity.noContent().build();
    }
}

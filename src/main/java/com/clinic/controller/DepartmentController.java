package com.clinic.controller;

import com.clinic.dto.ApiResponse;
import com.clinic.dto.request.DepartmentRequest;
import com.clinic.dto.response.DepartmentResponse;
import com.clinic.service.DepartmentService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Slf4j
@RestController
@Tag(name = "Departments", description = "Department management endpoints")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("/api/departments")
    @Operation(summary = "List all clinic departments (Public)")
    public ResponseEntity<ApiResponse<List<DepartmentResponse>>> getAllDepartments() {
        List<DepartmentResponse> list = departmentService.getAllDepartments();
        return ResponseEntity.ok(ApiResponse.success(list, "Departments retrieved successfully"));
    }

    @GetMapping("/api/departments/{id}")
    @Operation(summary = "Get department by ID")
    public ResponseEntity<ApiResponse<DepartmentResponse>> getDepartmentById(@PathVariable Long id) {
        DepartmentResponse response = departmentService.getDepartmentById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Department retrieved successfully"));
    }

    @PostMapping("/api/admin/departments")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Create a new department (Admin)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> createDepartment(@Valid @RequestBody DepartmentRequest request) {
        log.info("Admin creating department: {}", request.getName());
        DepartmentResponse response = departmentService.createDepartment(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.created(response, "Department created successfully"));
    }

    @PutMapping("/api/admin/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Update an existing department (Admin)")
    public ResponseEntity<ApiResponse<DepartmentResponse>> updateDepartment(
            @PathVariable Long id, @Valid @RequestBody DepartmentRequest request) {
        log.info("Admin updating department id: {}", id);
        DepartmentResponse response = departmentService.updateDepartment(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Department updated successfully"));
    }

    @DeleteMapping("/api/admin/departments/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(summary = "Delete department (Admin)")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        log.info("Admin deleting department id: {}", id);
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }
}

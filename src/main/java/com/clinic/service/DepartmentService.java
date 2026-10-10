package com.clinic.service;

import com.clinic.dto.request.DepartmentRequest;
import com.clinic.dto.response.DepartmentResponse;

import java.util.List;

public interface DepartmentService {
    DepartmentResponse createDepartment(DepartmentRequest request);
    DepartmentResponse updateDepartment(Long id, DepartmentRequest request);
    void deleteDepartment(Long id);
    List<DepartmentResponse> getAllDepartments();
    DepartmentResponse getDepartmentById(Long id);
}

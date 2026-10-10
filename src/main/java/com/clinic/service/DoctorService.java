package com.clinic.service;

import com.clinic.dto.PageResponse;
import com.clinic.dto.request.DoctorCreateRequest;
import com.clinic.dto.request.DoctorUpdateRequest;
import com.clinic.dto.response.DoctorResponse;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface DoctorService {
    DoctorResponse createDoctor(DoctorCreateRequest request);
    DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request);
    void deleteDoctor(Long id);
    DoctorResponse getDoctorById(Long id);
    DoctorResponse getDoctorByUserId(Long userId);
    PageResponse<DoctorResponse> getDoctors(Long departmentId, String specialization, String keyword, Boolean available, Pageable pageable);
    List<DoctorResponse> getAllDoctorsList();
}

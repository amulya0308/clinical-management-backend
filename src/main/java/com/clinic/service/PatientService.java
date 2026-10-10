package com.clinic.service;

import com.clinic.dto.PageResponse;
import com.clinic.dto.request.PatientUpdateRequest;
import com.clinic.dto.response.PatientResponse;
import org.springframework.data.domain.Pageable;

public interface PatientService {
    PatientResponse getPatientById(Long id);
    PatientResponse getPatientByUserId(Long userId);
    PatientResponse getCurrentPatientProfile();
    PatientResponse updateCurrentPatientProfile(PatientUpdateRequest request);
    PatientResponse updatePatient(Long id, PatientUpdateRequest request);
    void deletePatient(Long id);
    PageResponse<PatientResponse> getPatients(String keyword, Pageable pageable);
}

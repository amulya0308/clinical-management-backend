package com.clinic.service;

import com.clinic.dto.response.AdminDashboardResponse;
import com.clinic.dto.response.DoctorDashboardResponse;
import com.clinic.dto.response.PatientDashboardResponse;

public interface DashboardService {
    AdminDashboardResponse getAdminDashboard();
    DoctorDashboardResponse getDoctorDashboard();
    PatientDashboardResponse getPatientDashboard();
}

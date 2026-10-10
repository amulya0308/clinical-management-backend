package com.clinic.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AdminDashboardResponse {
    private long totalDoctors;
    private long totalPatients;
    private long totalAppointments;
    private long totalDepartments;
    private long pendingAppointments;
    private long confirmedAppointments;
    private long completedAppointments;
    private long cancelledAppointments;
    private List<AppointmentResponse> recentAppointments;
}

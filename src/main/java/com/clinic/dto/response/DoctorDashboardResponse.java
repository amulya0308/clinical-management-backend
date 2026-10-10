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
public class DoctorDashboardResponse {
    private long todayAppointmentsCount;
    private long totalAssignedAppointments;
    private long pendingAppointmentsCount;
    private long confirmedAppointmentsCount;
    private long completedAppointmentsCount;
    private List<AppointmentResponse> todayAppointments;
}

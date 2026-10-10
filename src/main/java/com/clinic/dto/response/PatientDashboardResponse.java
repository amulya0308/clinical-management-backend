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
public class PatientDashboardResponse {
    private long activeAppointmentsCount;
    private long pastAppointmentsCount;
    private long totalAppointmentsCount;
    private List<AppointmentResponse> recentAppointments;
}

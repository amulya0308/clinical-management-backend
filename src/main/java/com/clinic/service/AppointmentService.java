package com.clinic.service;

import com.clinic.dto.PageResponse;
import com.clinic.dto.request.AppointmentCreateRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.AppointmentStatus;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

public interface AppointmentService {
    AppointmentResponse bookAppointment(AppointmentCreateRequest request);
    PageResponse<AppointmentResponse> getPatientAppointments(AppointmentStatus status, LocalDate date, Pageable pageable);
    AppointmentResponse cancelPatientAppointment(Long appointmentId);

    PageResponse<AppointmentResponse> getDoctorAppointments(AppointmentStatus status, LocalDate date, Pageable pageable);
    AppointmentResponse updateDoctorAppointmentStatus(Long appointmentId, AppointmentStatus newStatus);

    PageResponse<AppointmentResponse> getAllAppointments(Long doctorId, AppointmentStatus status, LocalDate date, Pageable pageable);
    AppointmentResponse adminUpdateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus);

    AppointmentResponse getAppointmentById(Long id);
}

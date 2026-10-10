package com.clinic.service.impl;

import com.clinic.dto.response.AdminDashboardResponse;
import com.clinic.dto.response.DoctorDashboardResponse;
import com.clinic.dto.response.PatientDashboardResponse;
import com.clinic.entity.AppointmentStatus;
import com.clinic.entity.Doctor;
import com.clinic.entity.Patient;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.AppointmentMapper;
import com.clinic.repository.*;
import com.clinic.service.DashboardService;
import com.clinic.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentMapper appointmentMapper;

    public DashboardServiceImpl(
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            DepartmentRepository departmentRepository,
            AppointmentRepository appointmentRepository,
            AppointmentMapper appointmentMapper) {
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public AdminDashboardResponse getAdminDashboard() {
        return AdminDashboardResponse.builder()
                .totalDoctors(doctorRepository.count())
                .totalPatients(patientRepository.count())
                .totalAppointments(appointmentRepository.count())
                .totalDepartments(departmentRepository.count())
                .pendingAppointments(appointmentRepository.countByStatus(AppointmentStatus.PENDING))
                .confirmedAppointments(appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED))
                .completedAppointments(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED))
                .cancelledAppointments(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED))
                .recentAppointments(appointmentRepository.findTop5ByOrderByCreatedAtDesc()
                        .stream().map(appointmentMapper::toResponse).collect(Collectors.toList()))
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorDashboardResponse getDoctorDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user: " + userId));

        LocalDate today = LocalDate.now();
        long todayCount = appointmentRepository.countByDoctorIdAndAppointmentDate(doctor.getId(), today);
        long totalAssigned = appointmentRepository.countByDoctorId(doctor.getId());
        long pendingCount = appointmentRepository.countByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.PENDING);
        long confirmedCount = appointmentRepository.countByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.CONFIRMED);
        long completedCount = appointmentRepository.countByDoctorIdAndStatus(doctor.getId(), AppointmentStatus.COMPLETED);

        var todayList = appointmentRepository.findTop5ByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(doctor.getId(), today)
                .stream().map(appointmentMapper::toResponse).collect(Collectors.toList());

        return DoctorDashboardResponse.builder()
                .todayAppointmentsCount(todayCount)
                .totalAssignedAppointments(totalAssigned)
                .pendingAppointmentsCount(pendingCount)
                .confirmedAppointmentsCount(confirmedCount)
                .completedAppointmentsCount(completedCount)
                .todayAppointments(todayList)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public PatientDashboardResponse getPatientDashboard() {
        Long userId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user: " + userId));

        long activeCount = appointmentRepository.countByPatientIdAndStatusIn(
                patient.getId(), List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED));
        long pastCount = appointmentRepository.countByPatientIdAndStatusIn(
                patient.getId(), List.of(AppointmentStatus.COMPLETED, AppointmentStatus.CANCELLED));
        long totalCount = appointmentRepository.countByPatientId(patient.getId());

        var recentList = appointmentRepository.findTop5ByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(patient.getId())
                .stream().map(appointmentMapper::toResponse).collect(Collectors.toList());

        return PatientDashboardResponse.builder()
                .activeAppointmentsCount(activeCount)
                .pastAppointmentsCount(pastCount)
                .totalAppointmentsCount(totalCount)
                .recentAppointments(recentList)
                .build();
    }
}

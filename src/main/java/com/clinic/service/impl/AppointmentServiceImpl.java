package com.clinic.service.impl;

import com.clinic.dto.PageResponse;
import com.clinic.dto.request.AppointmentCreateRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.*;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ForbiddenException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.AppointmentMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.security.UserPrincipal;
import com.clinic.service.AppointmentService;
import com.clinic.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;

@Slf4j
@Service
public class AppointmentServiceImpl implements AppointmentService {

    private static final List<AppointmentStatus> ACTIVE_STATUSES =
            List.of(AppointmentStatus.PENDING, AppointmentStatus.CONFIRMED);

    private final AppointmentRepository appointmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentMapper appointmentMapper;

    public AppointmentServiceImpl(
            AppointmentRepository appointmentRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentMapper appointmentMapper) {
        this.appointmentRepository = appointmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentMapper = appointmentMapper;
    }

    @Override
    @Transactional
    public AppointmentResponse bookAppointment(AppointmentCreateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user id: " + userId));

        Doctor doctor = doctorRepository.findById(request.getDoctorId())
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + request.getDoctorId()));

        if (!doctor.isAvailable()) {
            throw new BadRequestException("Selected doctor " + doctor.getFullName() + " is currently unavailable for appointments.");
        }

        LocalDate appointmentDate = request.getAppointmentDate();
        LocalTime appointmentTime = request.getAppointmentTime();
        LocalDateTime appointmentDateTime = LocalDateTime.of(appointmentDate, appointmentTime);

        if (appointmentDateTime.isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Appointment cannot be booked in the past. Requested time: " + appointmentDateTime);
        }

        // Double booking check: no two active appointments for the same doctor at the same date and time
        boolean isConflict = appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
                doctor.getId(),
                appointmentDate,
                appointmentTime,
                ACTIVE_STATUSES
        );

        if (isConflict) {
            log.warn("Double booking conflict for doctor {} on {} at {}", doctor.getId(), appointmentDate, appointmentTime);
            throw new ConflictException("Doctor " + doctor.getFullName() + " is already booked at " +
                    appointmentDate + " " + appointmentTime + ". Please choose a different time slot.");
        }

        Appointment appointment = Appointment.builder()
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(appointmentDate)
                .appointmentTime(appointmentTime)
                .reason(request.getReason().trim())
                .status(AppointmentStatus.PENDING)
                .build();

        appointment = appointmentRepository.save(appointment);
        log.info("Appointment booked successfully with id: {} for patient {} and doctor {}",
                appointment.getId(), patient.getId(), doctor.getId());

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getPatientAppointments(AppointmentStatus status, LocalDate date, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user id: " + userId));

        Page<Appointment> page = appointmentRepository.findByPatientIdWithFilters(patient.getId(), status, date, pageable);
        return PageResponse.of(page.map(appointmentMapper::toResponse));
    }

    @Override
    @Transactional
    public AppointmentResponse cancelPatientAppointment(Long appointmentId) {
        Long userId = SecurityUtils.getCurrentUserId();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Ownership check
        if (!appointment.getPatient().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only cancel your own appointments.");
        }

        if (appointment.getStatus() != AppointmentStatus.PENDING &&
            appointment.getStatus() != AppointmentStatus.CONFIRMED) {
            throw new BadRequestException("Cannot cancel an appointment that is already " + appointment.getStatus());
        }

        appointment.setStatus(AppointmentStatus.CANCELLED);
        appointment = appointmentRepository.save(appointment);
        log.info("Patient {} cancelled appointment {}", userId, appointmentId);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getDoctorAppointments(AppointmentStatus status, LocalDate date, Pageable pageable) {
        Long userId = SecurityUtils.getCurrentUserId();
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));

        Page<Appointment> page = appointmentRepository.findByDoctorIdWithFilters(doctor.getId(), status, date, pageable);
        return PageResponse.of(page.map(appointmentMapper::toResponse));
    }

    @Override
    @Transactional
    public AppointmentResponse updateDoctorAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) {
        Long userId = SecurityUtils.getCurrentUserId();
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Ownership check
        if (!appointment.getDoctor().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only update appointments assigned to you.");
        }

        validateStatusTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);
        log.info("Doctor {} updated appointment {} to status {}", userId, appointmentId, newStatus);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<AppointmentResponse> getAllAppointments(Long doctorId, AppointmentStatus status, LocalDate date, Pageable pageable) {
        Page<Appointment> page = appointmentRepository.findAllWithFilters(doctorId, status, date, pageable);
        return PageResponse.of(page.map(appointmentMapper::toResponse));
    }

    @Override
    @Transactional
    public AppointmentResponse adminUpdateAppointmentStatus(Long appointmentId, AppointmentStatus newStatus) {
        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        validateStatusTransition(appointment.getStatus(), newStatus);

        appointment.setStatus(newStatus);
        appointment = appointmentRepository.save(appointment);
        log.info("Admin updated appointment {} to status {}", appointmentId, newStatus);

        return appointmentMapper.toResponse(appointment);
    }

    @Override
    @Transactional(readOnly = true)
    public AppointmentResponse getAppointmentById(Long id) {
        Appointment appointment = appointmentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + id));

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.PATIENT) {
            if (!appointment.getPatient().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You can only view your own appointments.");
            }
        } else if (currentUser.getRole() == Role.DOCTOR) {
            if (!appointment.getDoctor().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You can only view appointments assigned to you.");
            }
        }

        return appointmentMapper.toResponse(appointment);
    }

    private void validateStatusTransition(AppointmentStatus currentStatus, AppointmentStatus newStatus) {
        if (currentStatus == newStatus) {
            return;
        }

        if (currentStatus == AppointmentStatus.CANCELLED || currentStatus == AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Cannot alter appointment with terminal status: " + currentStatus);
        }

        if (currentStatus == AppointmentStatus.PENDING) {
            if (newStatus != AppointmentStatus.CONFIRMED && newStatus != AppointmentStatus.CANCELLED) {
                throw new BadRequestException("Invalid transition: PENDING can only move to CONFIRMED or CANCELLED.");
            }
        } else if (currentStatus == AppointmentStatus.CONFIRMED) {
            if (newStatus != AppointmentStatus.COMPLETED && newStatus != AppointmentStatus.CANCELLED) {
                throw new BadRequestException("Invalid transition: CONFIRMED can only move to COMPLETED or CANCELLED.");
            }
        }
    }
}

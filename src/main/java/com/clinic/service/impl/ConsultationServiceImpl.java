package com.clinic.service.impl;

import com.clinic.dto.request.ConsultationRequest;
import com.clinic.dto.response.ConsultationResponse;
import com.clinic.entity.Appointment;
import com.clinic.entity.AppointmentStatus;
import com.clinic.entity.Consultation;
import com.clinic.entity.Role;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ForbiddenException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.ConsultationMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.ConsultationRepository;
import com.clinic.security.UserPrincipal;
import com.clinic.service.ConsultationService;
import com.clinic.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class ConsultationServiceImpl implements ConsultationService {

    private final ConsultationRepository consultationRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationMapper consultationMapper;

    public ConsultationServiceImpl(
            ConsultationRepository consultationRepository,
            AppointmentRepository appointmentRepository,
            ConsultationMapper consultationMapper) {
        this.consultationRepository = consultationRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationMapper = consultationMapper;
    }

    @Override
    @Transactional
    public ConsultationResponse createConsultation(Long appointmentId, ConsultationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Creating consultation for appointment {} by user {}", appointmentId, userId);

        Appointment appointment = appointmentRepository.findById(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Appointment not found with id: " + appointmentId));

        // Doctor ownership check: appointment must belong to the doctor
        if (!appointment.getDoctor().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only create consultations for appointments assigned to you.");
        }

        // Status rule: only CONFIRMED or COMPLETED
        if (appointment.getStatus() != AppointmentStatus.CONFIRMED &&
            appointment.getStatus() != AppointmentStatus.COMPLETED) {
            throw new BadRequestException("Consultations can only be created for CONFIRMED appointments. Current status: " + appointment.getStatus());
        }

        // One-to-one rule: only once per appointment
        if (consultationRepository.existsByAppointmentId(appointmentId)) {
            throw new ConflictException("A consultation already exists for this appointment.");
        }

        Consultation consultation = Consultation.builder()
                .appointment(appointment)
                .diagnosis(request.getDiagnosis().trim())
                .prescription(request.getPrescription().trim())
                .notes(request.getNotes() != null ? request.getNotes().trim() : null)
                .build();

        consultation = consultationRepository.save(consultation);

        // Mark appointment as COMPLETED
        appointment.setStatus(AppointmentStatus.COMPLETED);
        appointmentRepository.save(appointment);

        log.info("Consultation created with id: {} and appointment {} marked COMPLETED", consultation.getId(), appointmentId);
        return consultationMapper.toResponse(consultation);
    }

    @Override
    @Transactional
    public ConsultationResponse updateConsultation(Long consultationId, ConsultationRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Updating consultation id: {} by user: {}", consultationId, userId);

        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + consultationId));

        if (!consultation.getAppointment().getDoctor().getUser().getId().equals(userId)) {
            throw new ForbiddenException("You can only modify consultations you created.");
        }

        consultation.setDiagnosis(request.getDiagnosis().trim());
        consultation.setPrescription(request.getPrescription().trim());
        consultation.setNotes(request.getNotes() != null ? request.getNotes().trim() : null);

        consultation = consultationRepository.save(consultation);
        return consultationMapper.toResponse(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationByAppointmentId(Long appointmentId) {
        Consultation consultation = consultationRepository.findByAppointmentId(appointmentId)
                .orElseThrow(() -> new ResourceNotFoundException("No consultation found for appointment id: " + appointmentId));

        checkConsultationAccess(consultation);
        return consultationMapper.toResponse(consultation);
    }

    @Override
    @Transactional(readOnly = true)
    public ConsultationResponse getConsultationById(Long consultationId) {
        Consultation consultation = consultationRepository.findById(consultationId)
                .orElseThrow(() -> new ResourceNotFoundException("Consultation not found with id: " + consultationId));

        checkConsultationAccess(consultation);
        return consultationMapper.toResponse(consultation);
    }

    private void checkConsultationAccess(Consultation consultation) {
        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.ADMIN) {
            return;
        }

        Appointment appt = consultation.getAppointment();
        if (currentUser.getRole() == Role.PATIENT) {
            if (!appt.getPatient().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You do not have permission to view this consultation.");
            }
        } else if (currentUser.getRole() == Role.DOCTOR) {
            if (!appt.getDoctor().getUser().getId().equals(currentUser.getId())) {
                throw new ForbiddenException("You do not have permission to view this consultation.");
            }
        }
    }
}

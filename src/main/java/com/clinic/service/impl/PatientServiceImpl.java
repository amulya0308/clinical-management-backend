package com.clinic.service.impl;

import com.clinic.dto.PageResponse;
import com.clinic.dto.request.PatientUpdateRequest;
import com.clinic.dto.response.PatientResponse;
import com.clinic.entity.Patient;
import com.clinic.entity.Role;
import com.clinic.entity.User;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ForbiddenException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.PatientMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.UserRepository;
import com.clinic.security.UserPrincipal;
import com.clinic.service.PatientService;
import com.clinic.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class PatientServiceImpl implements PatientService {

    private final PatientRepository patientRepository;
    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final PatientMapper patientMapper;

    public PatientServiceImpl(
            PatientRepository patientRepository,
            UserRepository userRepository,
            AppointmentRepository appointmentRepository,
            PatientMapper patientMapper) {
        this.patientRepository = patientRepository;
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.patientMapper = patientMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientById(Long id) {
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        UserPrincipal currentUser = SecurityUtils.getCurrentUser();
        if (currentUser.getRole() == Role.PATIENT && !patient.getUser().getId().equals(currentUser.getId())) {
            throw new ForbiddenException("You are not authorized to view this patient profile");
        }

        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getPatientByUserId(Long userId) {
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for user id: " + userId));
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional(readOnly = true)
    public PatientResponse getCurrentPatientProfile() {
        Long userId = SecurityUtils.getCurrentUserId();
        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public PatientResponse updateCurrentPatientProfile(PatientUpdateRequest request) {
        Long userId = SecurityUtils.getCurrentUserId();
        log.info("Updating profile for current user id: {}", userId);

        Patient patient = patientRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient profile not found for current user"));

        return updatePatientData(patient, request);
    }

    @Override
    @Transactional
    public PatientResponse updatePatient(Long id, PatientUpdateRequest request) {
        log.info("Admin updating patient id: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        return updatePatientData(patient, request);
    }

    private PatientResponse updatePatientData(Patient patient, PatientUpdateRequest request) {
        patient.setFullName(request.getFullName().trim());
        patient.setAge(request.getAge());
        patient.setGender(request.getGender().trim());
        patient.setPhone(request.getPhone().trim());
        patient.setAddress(request.getAddress().trim());

        patient = patientRepository.save(patient);
        return patientMapper.toResponse(patient);
    }

    @Override
    @Transactional
    public void deletePatient(Long id) {
        log.info("Attempting to delete patient id: {}", id);
        Patient patient = patientRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + id));

        if (appointmentRepository.existsByPatientId(id)) {
            throw new ConflictException("Cannot delete patient with existing appointment history.");
        }

        User user = patient.getUser();
        patientRepository.delete(patient);
        if (user != null) {
            userRepository.delete(user);
        }
        log.info("Successfully deleted patient id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<PatientResponse> getPatients(String keyword, Pageable pageable) {
        Page<Patient> page = patientRepository.findWithFilters(keyword, pageable);
        return PageResponse.of(page.map(patientMapper::toResponse));
    }
}

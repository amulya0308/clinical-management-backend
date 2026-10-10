package com.clinic.service;

import com.clinic.dto.request.ConsultationRequest;
import com.clinic.dto.response.ConsultationResponse;
import com.clinic.entity.*;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ForbiddenException;
import com.clinic.mapper.ConsultationMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.ConsultationRepository;
import com.clinic.security.UserPrincipal;
import com.clinic.service.impl.ConsultationServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsultationServiceTest {

    @Mock
    private ConsultationRepository consultationRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private ConsultationMapper consultationMapper;

    @InjectMocks
    private ConsultationServiceImpl consultationService;

    private User doctorUser;
    private Doctor doctor;
    private Appointment appointment;

    @BeforeEach
    void setUp() {
        doctorUser = User.builder().id(2L).email("doctor@clinic.com").role(Role.DOCTOR).enabled(true).build();
        doctor = Doctor.builder().id(20L).user(doctorUser).fullName("Dr. Smith").build();

        User patientUser = User.builder().id(1L).email("patient@clinic.com").role(Role.PATIENT).enabled(true).build();
        Patient patient = Patient.builder().id(10L).user(patientUser).fullName("John Doe").build();

        appointment = Appointment.builder()
                .id(100L)
                .doctor(doctor)
                .patient(patient)
                .status(AppointmentStatus.CONFIRMED)
                .build();

        UserPrincipal doctorPrincipal = UserPrincipal.create(doctorUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(doctorPrincipal, null, doctorPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testCreateConsultation_SuccessMarksAppointmentCompleted() {
        ConsultationRequest request = ConsultationRequest.builder()
                .diagnosis("Hypertension")
                .prescription("Medication A")
                .notes("Follow up in 2 weeks")
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(consultationRepository.existsByAppointmentId(100L)).thenReturn(false);

        Consultation saved = Consultation.builder()
                .id(50L)
                .appointment(appointment)
                .diagnosis("Hypertension")
                .prescription("Medication A")
                .build();

        when(consultationRepository.save(any(Consultation.class))).thenReturn(saved);
        when(consultationMapper.toResponse(saved)).thenReturn(ConsultationResponse.builder().id(50L).build());

        ConsultationResponse response = consultationService.createConsultation(100L, request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.COMPLETED, appointment.getStatus());
        verify(appointmentRepository, times(1)).save(appointment);
    }

    @Test
    void testCreateConsultation_DuplicateThrowsConflict() {
        ConsultationRequest request = ConsultationRequest.builder()
                .diagnosis("Hypertension")
                .prescription("Medication A")
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));
        when(consultationRepository.existsByAppointmentId(100L)).thenReturn(true);

        assertThrows(ConflictException.class, () -> consultationService.createConsultation(100L, request));
    }

    @Test
    void testCreateConsultation_PendingStatusThrowsBadRequest() {
        appointment.setStatus(AppointmentStatus.PENDING);
        ConsultationRequest request = ConsultationRequest.builder()
                .diagnosis("Hypertension")
                .prescription("Medication A")
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(BadRequestException.class, () -> consultationService.createConsultation(100L, request));
    }

    @Test
    void testCreateConsultation_DifferentDoctorThrowsForbidden() {
        User otherDoctorUser = User.builder().id(999L).email("otherdoc@clinic.com").role(Role.DOCTOR).enabled(true).build();
        UserPrincipal otherDoctorPrincipal = UserPrincipal.create(otherDoctorUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(otherDoctorPrincipal, null, otherDoctorPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        ConsultationRequest request = ConsultationRequest.builder()
                .diagnosis("Hypertension")
                .prescription("Medication A")
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(ForbiddenException.class, () -> consultationService.createConsultation(100L, request));
    }
}

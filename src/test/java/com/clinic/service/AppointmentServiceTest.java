package com.clinic.service;

import com.clinic.dto.request.AppointmentCreateRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.*;
import com.clinic.exception.BadRequestException;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ForbiddenException;
import com.clinic.mapper.AppointmentMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.security.UserPrincipal;
import com.clinic.service.impl.AppointmentServiceImpl;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentServiceTest {

    @Mock
    private AppointmentRepository appointmentRepository;

    @Mock
    private DoctorRepository doctorRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentMapper appointmentMapper;

    @InjectMocks
    private AppointmentServiceImpl appointmentService;

    private User patientUser;
    private User doctorUser;
    private Patient patient;
    private Doctor doctor;

    @BeforeEach
    void setUp() {
        patientUser = User.builder().id(1L).email("patient@clinic.com").role(Role.PATIENT).enabled(true).build();
        doctorUser = User.builder().id(2L).email("doctor@clinic.com").role(Role.DOCTOR).enabled(true).build();

        Department department = Department.builder().id(1L).name("Cardiology").build();

        patient = Patient.builder().id(10L).user(patientUser).fullName("John Doe").phone("123456").address("City").build();
        doctor = Doctor.builder().id(20L).user(doctorUser).department(department).fullName("Dr. Smith").available(true).build();

        UserPrincipal principal = UserPrincipal.create(patientUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(principal, null, principal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    @Test
    void testBookAppointment_Success() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(20L)
                .appointmentDate(futureDate)
                .appointmentTime(time)
                .reason("Chest checkup")
                .build();

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
                eq(20L), eq(futureDate), eq(time), anyCollection())).thenReturn(false);

        Appointment savedAppointment = Appointment.builder()
                .id(100L)
                .patient(patient)
                .doctor(doctor)
                .appointmentDate(futureDate)
                .appointmentTime(time)
                .reason("Chest checkup")
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.save(any(Appointment.class))).thenReturn(savedAppointment);
        when(appointmentMapper.toResponse(savedAppointment)).thenReturn(
                AppointmentResponse.builder().id(100L).status(AppointmentStatus.PENDING).build());

        AppointmentResponse response = appointmentService.bookAppointment(request);

        assertNotNull(response);
        assertEquals(AppointmentStatus.PENDING, response.getStatus());
        verify(appointmentRepository, times(1)).save(any(Appointment.class));
    }

    @Test
    void testBookAppointment_DoubleBookingThrowsConflict() {
        LocalDate futureDate = LocalDate.now().plusDays(2);
        LocalTime time = LocalTime.of(10, 0);

        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(20L)
                .appointmentDate(futureDate)
                .appointmentTime(time)
                .reason("Checkup")
                .build();

        when(patientRepository.findByUserId(1L)).thenReturn(Optional.of(patient));
        when(doctorRepository.findById(20L)).thenReturn(Optional.of(doctor));
        // Doctor already has active appointment at this date and time
        when(appointmentRepository.existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
                eq(20L), eq(futureDate), eq(time), anyCollection())).thenReturn(true);

        assertThrows(ConflictException.class, () -> appointmentService.bookAppointment(request));
        verify(appointmentRepository, never()).save(any(Appointment.class));
    }

    @Test
    void testCancelPatientAppointment_ForbiddenWhenNotOwner() {
        User otherUser = User.builder().id(99L).email("other@clinic.com").role(Role.PATIENT).enabled(true).build();
        Patient otherPatient = Patient.builder().id(99L).user(otherUser).fullName("Other").build();

        Appointment appointment = Appointment.builder()
                .id(100L)
                .patient(otherPatient)
                .doctor(doctor)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        // Current user in context has ID 1L, but appointment belongs to user 99L
        assertThrows(ForbiddenException.class, () -> appointmentService.cancelPatientAppointment(100L));
    }

    @Test
    void testCancelPatientAppointment_BadRequestWhenAlreadyCompleted() {
        Appointment appointment = Appointment.builder()
                .id(100L)
                .patient(patient)
                .doctor(doctor)
                .status(AppointmentStatus.COMPLETED)
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(BadRequestException.class, () -> appointmentService.cancelPatientAppointment(100L));
    }

    @Test
    void testDoctorStatusTransition_InvalidFromCompletedThrowsException() {
        UserPrincipal doctorPrincipal = UserPrincipal.create(doctorUser);
        UsernamePasswordAuthenticationToken auth =
                new UsernamePasswordAuthenticationToken(doctorPrincipal, null, doctorPrincipal.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(auth);

        Appointment appointment = Appointment.builder()
                .id(100L)
                .patient(patient)
                .doctor(doctor)
                .status(AppointmentStatus.COMPLETED)
                .build();

        when(appointmentRepository.findById(100L)).thenReturn(Optional.of(appointment));

        assertThrows(BadRequestException.class,
                () -> appointmentService.updateDoctorAppointmentStatus(100L, AppointmentStatus.CONFIRMED));
    }
}

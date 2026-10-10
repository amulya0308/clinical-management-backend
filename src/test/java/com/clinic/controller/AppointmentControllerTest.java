package com.clinic.controller;

import com.clinic.dto.request.AppointmentCreateRequest;
import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.AppointmentStatus;
import com.clinic.service.AppointmentService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.time.LocalTime;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AppointmentControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AppointmentService appointmentService;

    @Test
    @WithMockUser(username = "patient@clinic.com", roles = {"PATIENT"})
    void testBookAppointment_AuthenticatedPatient_Success() throws Exception {
        LocalDate date = LocalDate.now().plusDays(3);
        LocalTime time = LocalTime.of(14, 0);

        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(1L)
                .appointmentDate(date)
                .appointmentTime(time)
                .reason("Consultation for back ache")
                .build();

        AppointmentResponse response = AppointmentResponse.builder()
                .id(10L)
                .doctorId(1L)
                .doctorName("Dr. Sarah")
                .appointmentDate(date)
                .appointmentTime(time)
                .status(AppointmentStatus.PENDING)
                .build();

        when(appointmentService.bookAppointment(any(AppointmentCreateRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/patient/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.status").value(201))
                .andExpect(jsonPath("$.data.id").value(10L))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    void testBookAppointment_Unauthenticated_ForbiddenOrUnauthorized() throws Exception {
        AppointmentCreateRequest request = AppointmentCreateRequest.builder()
                .doctorId(1L)
                .appointmentDate(LocalDate.now().plusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .reason("Checkup")
                .build();

        mockMvc.perform(post("/api/patient/appointments")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }
}

package com.clinic.mapper;

import com.clinic.dto.response.AppointmentResponse;
import com.clinic.entity.Appointment;
import org.springframework.stereotype.Component;

@Component
public class AppointmentMapper {

    public AppointmentResponse toResponse(Appointment appointment) {
        if (appointment == null) return null;
        boolean hasConsultation = appointment.getConsultation() != null;
        Long consultationId = hasConsultation ? appointment.getConsultation().getId() : null;

        return AppointmentResponse.builder()
                .id(appointment.getId())
                .patientId(appointment.getPatient() != null ? appointment.getPatient().getId() : null)
                .patientName(appointment.getPatient() != null ? appointment.getPatient().getFullName() : null)
                .patientPhone(appointment.getPatient() != null ? appointment.getPatient().getPhone() : null)
                .doctorId(appointment.getDoctor() != null ? appointment.getDoctor().getId() : null)
                .doctorName(appointment.getDoctor() != null ? appointment.getDoctor().getFullName() : null)
                .doctorSpecialization(appointment.getDoctor() != null ? appointment.getDoctor().getSpecialization() : null)
                .departmentId(appointment.getDoctor() != null && appointment.getDoctor().getDepartment() != null
                        ? appointment.getDoctor().getDepartment().getId() : null)
                .departmentName(appointment.getDoctor() != null && appointment.getDoctor().getDepartment() != null
                        ? appointment.getDoctor().getDepartment().getName() : null)
                .appointmentDate(appointment.getAppointmentDate())
                .appointmentTime(appointment.getAppointmentTime())
                .reason(appointment.getReason())
                .status(appointment.getStatus())
                .hasConsultation(hasConsultation)
                .consultationId(consultationId)
                .createdAt(appointment.getCreatedAt())
                .build();
    }
}

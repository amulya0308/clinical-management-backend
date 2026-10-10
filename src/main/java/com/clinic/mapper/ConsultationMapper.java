package com.clinic.mapper;

import com.clinic.dto.response.ConsultationResponse;
import com.clinic.entity.Appointment;
import com.clinic.entity.Consultation;
import org.springframework.stereotype.Component;

@Component
public class ConsultationMapper {

    public ConsultationResponse toResponse(Consultation consultation) {
        if (consultation == null) return null;
        Appointment appt = consultation.getAppointment();

        return ConsultationResponse.builder()
                .id(consultation.getId())
                .appointmentId(appt != null ? appt.getId() : null)
                .doctorId(appt != null && appt.getDoctor() != null ? appt.getDoctor().getId() : null)
                .doctorName(appt != null && appt.getDoctor() != null ? appt.getDoctor().getFullName() : null)
                .doctorSpecialization(appt != null && appt.getDoctor() != null ? appt.getDoctor().getSpecialization() : null)
                .departmentName(appt != null && appt.getDoctor() != null && appt.getDoctor().getDepartment() != null
                        ? appt.getDoctor().getDepartment().getName() : null)
                .patientId(appt != null && appt.getPatient() != null ? appt.getPatient().getId() : null)
                .patientName(appt != null && appt.getPatient() != null ? appt.getPatient().getFullName() : null)
                .patientAge(appt != null && appt.getPatient() != null ? appt.getPatient().getAge() : null)
                .patientGender(appt != null && appt.getPatient() != null ? appt.getPatient().getGender() : null)
                .appointmentDate(appt != null ? appt.getAppointmentDate() : null)
                .appointmentTime(appt != null ? appt.getAppointmentTime() : null)
                .diagnosis(consultation.getDiagnosis())
                .prescription(consultation.getPrescription())
                .notes(consultation.getNotes())
                .createdAt(consultation.getCreatedAt())
                .build();
    }
}

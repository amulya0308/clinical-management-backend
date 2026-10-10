package com.clinic.service;

import com.clinic.dto.request.ConsultationRequest;
import com.clinic.dto.response.ConsultationResponse;

public interface ConsultationService {
    ConsultationResponse createConsultation(Long appointmentId, ConsultationRequest request);
    ConsultationResponse updateConsultation(Long consultationId, ConsultationRequest request);
    ConsultationResponse getConsultationByAppointmentId(Long appointmentId);
    ConsultationResponse getConsultationById(Long consultationId);
}

package com.clinic.repository;

import com.clinic.entity.Consultation;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ConsultationRepository extends JpaRepository<Consultation, Long> {
    Optional<Consultation> findByAppointmentId(Long appointmentId);
    boolean existsByAppointmentId(Long appointmentId);
}

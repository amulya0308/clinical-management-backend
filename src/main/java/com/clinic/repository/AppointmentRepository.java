package com.clinic.repository;

import com.clinic.entity.Appointment;
import com.clinic.entity.AppointmentStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Collection;
import java.util.List;

@Repository
public interface AppointmentRepository extends JpaRepository<Appointment, Long> {

    boolean existsByDoctorIdAndAppointmentDateAndAppointmentTimeAndStatusIn(
            Long doctorId,
            LocalDate appointmentDate,
            LocalTime appointmentTime,
            Collection<AppointmentStatus> statuses
    );

    boolean existsByDoctorId(Long doctorId);
    boolean existsByPatientId(Long patientId);

    long countByDoctorId(Long doctorId);
    long countByPatientId(Long patientId);
    long countByStatus(AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.status = :status")
    long countByDoctorIdAndStatus(@Param("doctorId") Long doctorId, @Param("status") AppointmentStatus status);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.doctor.id = :doctorId AND a.appointmentDate = :date")
    long countByDoctorIdAndAppointmentDate(@Param("doctorId") Long doctorId, @Param("date") LocalDate date);

    @Query("SELECT COUNT(a) FROM Appointment a WHERE a.patient.id = :patientId AND a.status IN :statuses")
    long countByPatientIdAndStatusIn(@Param("patientId") Long patientId, @Param("statuses") Collection<AppointmentStatus> statuses);

    @Query("SELECT a FROM Appointment a WHERE a.patient.id = :patientId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:date IS NULL OR a.appointmentDate = :date)")
    Page<Appointment> findByPatientIdWithFilters(@Param("patientId") Long patientId,
                                                @Param("status") AppointmentStatus status,
                                                @Param("date") LocalDate date,
                                                Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE a.doctor.id = :doctorId " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:date IS NULL OR a.appointmentDate = :date)")
    Page<Appointment> findByDoctorIdWithFilters(@Param("doctorId") Long doctorId,
                                               @Param("status") AppointmentStatus status,
                                               @Param("date") LocalDate date,
                                               Pageable pageable);

    @Query("SELECT a FROM Appointment a WHERE " +
           "(:doctorId IS NULL OR a.doctor.id = :doctorId) AND " +
           "(:status IS NULL OR a.status = :status) AND " +
           "(:date IS NULL OR a.appointmentDate = :date)")
    Page<Appointment> findAllWithFilters(@Param("doctorId") Long doctorId,
                                        @Param("status") AppointmentStatus status,
                                        @Param("date") LocalDate date,
                                        Pageable pageable);

    List<Appointment> findTop5ByDoctorIdAndAppointmentDateOrderByAppointmentTimeAsc(Long doctorId, LocalDate date);

    List<Appointment> findTop5ByOrderByCreatedAtDesc();

    List<Appointment> findTop5ByPatientIdOrderByAppointmentDateDescAppointmentTimeDesc(Long patientId);
}

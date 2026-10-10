package com.clinic.repository;

import com.clinic.entity.Patient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface PatientRepository extends JpaRepository<Patient, Long> {
    Optional<Patient> findByUserId(Long userId);
    Optional<Patient> findByUserEmail(String email);

    @Query("SELECT p FROM Patient p WHERE " +
           "(:keyword IS NULL OR LOWER(p.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.phone) LIKE LOWER(CONCAT('%', :keyword, '%')) OR " +
           "LOWER(p.user.email) LIKE LOWER(CONCAT('%', :keyword, '%')))")
    Page<Patient> findWithFilters(@Param("keyword") String keyword, Pageable pageable);
}

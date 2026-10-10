package com.clinic.repository;

import com.clinic.entity.Doctor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface DoctorRepository extends JpaRepository<Doctor, Long> {
    Optional<Doctor> findByUserId(Long userId);
    Optional<Doctor> findByUserEmail(String email);
    boolean existsByDepartmentId(Long departmentId);
    long countByDepartmentId(Long departmentId);

    @Query("SELECT d FROM Doctor d WHERE " +
           "(:departmentId IS NULL OR d.department.id = :departmentId) AND " +
           "(:specialization IS NULL OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :specialization, '%'))) AND " +
           "(:keyword IS NULL OR LOWER(d.fullName) LIKE LOWER(CONCAT('%', :keyword, '%')) OR LOWER(d.specialization) LIKE LOWER(CONCAT('%', :keyword, '%'))) AND " +
           "(:available IS NULL OR d.available = :available)")
    Page<Doctor> findWithFilters(@Param("departmentId") Long departmentId,
                                @Param("specialization") String specialization,
                                @Param("keyword") String keyword,
                                @Param("available") Boolean available,
                                Pageable pageable);
}

package com.clinic.service.impl;

import com.clinic.dto.PageResponse;
import com.clinic.dto.request.DoctorCreateRequest;
import com.clinic.dto.request.DoctorUpdateRequest;
import com.clinic.dto.response.DoctorResponse;
import com.clinic.entity.Department;
import com.clinic.entity.Doctor;
import com.clinic.entity.Role;
import com.clinic.entity.User;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.DoctorMapper;
import com.clinic.repository.AppointmentRepository;
import com.clinic.repository.DepartmentRepository;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.UserRepository;
import com.clinic.service.DoctorService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class DoctorServiceImpl implements DoctorService {

    private final DoctorRepository doctorRepository;
    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final AppointmentRepository appointmentRepository;
    private final PasswordEncoder passwordEncoder;
    private final DoctorMapper doctorMapper;

    public DoctorServiceImpl(
            DoctorRepository doctorRepository,
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            AppointmentRepository appointmentRepository,
            PasswordEncoder passwordEncoder,
            DoctorMapper doctorMapper) {
        this.doctorRepository = doctorRepository;
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.appointmentRepository = appointmentRepository;
        this.passwordEncoder = passwordEncoder;
        this.doctorMapper = doctorMapper;
    }

    @Override
    @Transactional
    public DoctorResponse createDoctor(DoctorCreateRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Creating doctor with email: {}", email);

        if (userRepository.existsByEmail(email)) {
            throw new ConflictException("Email already exists: " + email);
        }

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        User user = User.builder()
                .email(email)
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DOCTOR)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Doctor doctor = Doctor.builder()
                .user(user)
                .department(department)
                .fullName(request.getFullName().trim())
                .specialization(request.getSpecialization().trim())
                .phone(request.getPhone().trim())
                .experienceYears(request.getExperienceYears())
                .available(true)
                .build();
        doctor = doctorRepository.save(doctor);

        log.info("Created doctor with id: {}", doctor.getId());
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public DoctorResponse updateDoctor(Long id, DoctorUpdateRequest request) {
        log.info("Updating doctor with id: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        Department department = departmentRepository.findById(request.getDepartmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Department not found with id: " + request.getDepartmentId()));

        doctor.setDepartment(department);
        doctor.setFullName(request.getFullName().trim());
        doctor.setSpecialization(request.getSpecialization().trim());
        doctor.setPhone(request.getPhone().trim());
        doctor.setExperienceYears(request.getExperienceYears());
        if (request.getAvailable() != null) {
            doctor.setAvailable(request.getAvailable());
        }

        doctor = doctorRepository.save(doctor);
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional
    public void deleteDoctor(Long id) {
        log.info("Attempting to delete doctor id: {}", id);
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));

        if (appointmentRepository.existsByDoctorId(id)) {
            throw new ConflictException("Cannot delete doctor with existing appointments. Please deactivate availability instead.");
        }

        User user = doctor.getUser();
        doctorRepository.delete(doctor);
        if (user != null) {
            userRepository.delete(user);
        }
        log.info("Successfully deleted doctor id: {}", id);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorById(Long id) {
        Doctor doctor = doctorRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor not found with id: " + id));
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public DoctorResponse getDoctorByUserId(Long userId) {
        Doctor doctor = doctorRepository.findByUserId(userId)
                .orElseThrow(() -> new ResourceNotFoundException("Doctor profile not found for user id: " + userId));
        return doctorMapper.toResponse(doctor);
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<DoctorResponse> getDoctors(Long departmentId, String specialization, String keyword, Boolean available, Pageable pageable) {
        Page<Doctor> page = doctorRepository.findWithFilters(departmentId, specialization, keyword, available, pageable);
        Page<DoctorResponse> dtoPage = page.map(doctorMapper::toResponse);
        return PageResponse.of(dtoPage);
    }

    @Override
    @Transactional(readOnly = true)
    public List<DoctorResponse> getAllDoctorsList() {
        return doctorRepository.findAll().stream()
                .filter(Doctor::isAvailable)
                .map(doctorMapper::toResponse)
                .collect(Collectors.toList());
    }
}

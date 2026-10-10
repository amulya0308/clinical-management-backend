package com.clinic.service.impl;

import com.clinic.dto.request.LoginRequest;
import com.clinic.dto.request.RegisterRequest;
import com.clinic.dto.response.AuthResponse;
import com.clinic.dto.response.UserSummaryResponse;
import com.clinic.entity.Doctor;
import com.clinic.entity.Patient;
import com.clinic.entity.Role;
import com.clinic.entity.User;
import com.clinic.exception.ConflictException;
import com.clinic.exception.ResourceNotFoundException;
import com.clinic.mapper.UserMapper;
import com.clinic.repository.DoctorRepository;
import com.clinic.repository.PatientRepository;
import com.clinic.repository.UserRepository;
import com.clinic.security.JwtUtil;
import com.clinic.security.UserPrincipal;
import com.clinic.service.AuthService;
import com.clinic.util.SecurityUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PatientRepository patientRepository;
    private final DoctorRepository doctorRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtUtil jwtUtil;
    private final UserMapper userMapper;

    public AuthServiceImpl(
            UserRepository userRepository,
            PatientRepository patientRepository,
            DoctorRepository doctorRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtUtil jwtUtil,
            UserMapper userMapper) {
        this.userRepository = userRepository;
        this.patientRepository = patientRepository;
        this.doctorRepository = doctorRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtUtil = jwtUtil;
        this.userMapper = userMapper;
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest request) {
        log.info("Registering new patient with email: {}", request.getEmail());
        if (userRepository.existsByEmail(request.getEmail().trim().toLowerCase())) {
            throw new ConflictException("Email is already registered: " + request.getEmail());
        }

        User user = User.builder()
                .email(request.getEmail().trim().toLowerCase())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.PATIENT)
                .enabled(true)
                .build();
        user = userRepository.save(user);

        Patient patient = Patient.builder()
                .user(user)
                .fullName(request.getFullName().trim())
                .age(request.getAge())
                .gender(request.getGender())
                .phone(request.getPhone().trim())
                .address(request.getAddress().trim())
                .build();
        patient = patientRepository.save(patient);

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());

        log.info("Patient registered successfully with id: {}", patient.getId());
        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(patient.getFullName())
                .profileId(patient.getId())
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {
        String email = request.getEmail().trim().toLowerCase();
        log.info("Attempting login for user: {}", email);

        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(email, request.getPassword())
        );

        UserPrincipal principal = (UserPrincipal) authentication.getPrincipal();
        User user = userRepository.findByEmail(principal.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        String fullName = user.getEmail();
        Long profileId = null;

        if (user.getRole() == Role.PATIENT) {
            Patient patient = patientRepository.findByUserId(user.getId()).orElse(null);
            if (patient != null) {
                fullName = patient.getFullName();
                profileId = patient.getId();
            }
        } else if (user.getRole() == Role.DOCTOR) {
            Doctor doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
            if (doctor != null) {
                fullName = doctor.getFullName();
                profileId = doctor.getId();
            }
        } else if (user.getRole() == Role.ADMIN) {
            fullName = "System Administrator";
        }

        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name(), user.getId());
        log.info("Login successful for user: {} with role: {}", email, user.getRole());

        return AuthResponse.builder()
                .token(token)
                .type("Bearer")
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole())
                .fullName(fullName)
                .profileId(profileId)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public UserSummaryResponse getCurrentUserProfile() {
        String email = SecurityUtils.getCurrentUserEmail();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + email));

        Doctor doctor = null;
        Patient patient = null;

        if (user.getRole() == Role.DOCTOR) {
            doctor = doctorRepository.findByUserId(user.getId()).orElse(null);
        } else if (user.getRole() == Role.PATIENT) {
            patient = patientRepository.findByUserId(user.getId()).orElse(null);
        }

        return userMapper.toResponse(user, doctor, patient);
    }
}

package com.clinic.config;

import com.clinic.entity.*;
import com.clinic.repository.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;

@Slf4j
@Component
public class DataSeeder implements CommandLineRunner {

    private final UserRepository userRepository;
    private final DepartmentRepository departmentRepository;
    private final DoctorRepository doctorRepository;
    private final PatientRepository patientRepository;
    private final AppointmentRepository appointmentRepository;
    private final ConsultationRepository consultationRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(
            UserRepository userRepository,
            DepartmentRepository departmentRepository,
            DoctorRepository doctorRepository,
            PatientRepository patientRepository,
            AppointmentRepository appointmentRepository,
            ConsultationRepository consultationRepository,
            PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.departmentRepository = departmentRepository;
        this.doctorRepository = doctorRepository;
        this.patientRepository = patientRepository;
        this.appointmentRepository = appointmentRepository;
        this.consultationRepository = consultationRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            log.info("Database already contains users. Skipping initial data seeding.");
            return;
        }

        log.info("Starting initial clinical database seeding...");

        // 1. Seed Admin
        User adminUser = User.builder()
                .email("admin@clinic.com")
                .password(passwordEncoder.encode("Admin@123"))
                .role(Role.ADMIN)
                .enabled(true)
                .build();
        userRepository.save(adminUser);
        log.info("Seeded Admin: admin@clinic.com / Admin@123");

        // 2. Seed 4 Departments
        Department cardiology = departmentRepository.save(Department.builder()
                .name("Cardiology")
                .description("Specialized diagnosis and treatment of cardiovascular diseases, heart health and vascular conditions.")
                .build());

        Department dermatology = departmentRepository.save(Department.builder()
                .name("Dermatology")
                .description("Comprehensive medical, surgical and cosmetic care for skin, hair and nail conditions.")
                .build());

        Department pediatrics = departmentRepository.save(Department.builder()
                .name("Pediatrics")
                .description("Medical care and developmental wellness for infants, children and adolescents.")
                .build());

        Department orthopedics = departmentRepository.save(Department.builder()
                .name("Orthopedics")
                .description("Care for musculoskeletal system including joints, bones, spine and sports injuries.")
                .build());
        log.info("Seeded 4 departments");

        // 3. Seed 5 Doctors
        Doctor doc1 = seedDoctor("doctor1@clinic.com", "Doctor@123", "Dr. Sarah Jenkins", cardiology, "Senior Cardiologist", "+1-555-0201", 12);
        Doctor doc2 = seedDoctor("doctor2@clinic.com", "Doctor@123", "Dr. Marcus Vance", dermatology, "Dermatologist & Aesthetician", "+1-555-0202", 8);
        Doctor doc3 = seedDoctor("doctor3@clinic.com", "Doctor@123", "Dr. Elena Rostova", pediatrics, "Pediatric Specialist", "+1-555-0203", 10);
        Doctor doc4 = seedDoctor("doctor4@clinic.com", "Doctor@123", "Dr. David Chen", orthopedics, "Orthopedic Surgeon", "+1-555-0204", 15);
        Doctor doc5 = seedDoctor("doctor5@clinic.com", "Doctor@123", "Dr. Maya Patel", cardiology, "Interventional Cardiologist", "+1-555-0205", 7);
        log.info("Seeded 5 doctors");

        // 4. Seed 2 Sample Patients
        Patient pat1 = seedPatient("patient1@clinic.com", "Patient@123", "John Doe", 35, "Male", "+1-555-0101", "123 Elm St, Metropolis");
        Patient pat2 = seedPatient("patient2@clinic.com", "Patient@123", "Jane Smith", 28, "Female", "+1-555-0102", "456 Oak Ave, Metropolis");
        log.info("Seeded 2 patients");

        // 5. Seed Sample Appointments & Consultation
        LocalDate today = LocalDate.now();

        // Appointment 1: Completed with consultation
        Appointment appt1 = appointmentRepository.save(Appointment.builder()
                .patient(pat1)
                .doctor(doc1)
                .appointmentDate(today.minusDays(1))
                .appointmentTime(LocalTime.of(10, 0))
                .reason("Regular cardiac checkup and hypertension review")
                .status(AppointmentStatus.COMPLETED)
                .build());

        consultationRepository.save(Consultation.builder()
                .appointment(appt1)
                .diagnosis("Stage 1 primary hypertension, mild sinus tachycardia")
                .prescription("Amlodipine 5mg once daily morning, lifestyle sodium reduction")
                .notes("Blood pressure 138/88 mmHg. Recheck in 4 weeks.")
                .build());

        // Appointment 2: Confirmed today
        appointmentRepository.save(Appointment.builder()
                .patient(pat1)
                .doctor(doc1)
                .appointmentDate(today)
                .appointmentTime(LocalTime.of(14, 30))
                .reason("Follow-up blood pressure monitoring")
                .status(AppointmentStatus.CONFIRMED)
                .build());

        // Appointment 3: Pending future
        appointmentRepository.save(Appointment.builder()
                .patient(pat2)
                .doctor(doc2)
                .appointmentDate(today.plusDays(2))
                .appointmentTime(LocalTime.of(11, 0))
                .reason("Skin rash consultation and allergy evaluation")
                .status(AppointmentStatus.PENDING)
                .build());

        log.info("Database seeding completed successfully!");
    }

    private Doctor seedDoctor(String email, String password, String fullName, Department department,
                              String specialization, String phone, int experienceYears) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.DOCTOR)
                .enabled(true)
                .build());

        return doctorRepository.save(Doctor.builder()
                .user(user)
                .department(department)
                .fullName(fullName)
                .specialization(specialization)
                .phone(phone)
                .experienceYears(experienceYears)
                .available(true)
                .build());
    }

    private Patient seedPatient(String email, String password, String fullName,
                                int age, String gender, String phone, String address) {
        User user = userRepository.save(User.builder()
                .email(email)
                .password(passwordEncoder.encode(password))
                .role(Role.PATIENT)
                .enabled(true)
                .build());

        return patientRepository.save(Patient.builder()
                .user(user)
                .fullName(fullName)
                .age(age)
                .gender(gender)
                .phone(phone)
                .address(address)
                .build());
    }
}

package com.ignou.smartmedical.service;

// Implements: Authentication and Authorization Module — synopsis section 9.1

import com.ignou.smartmedical.dto.request.LoginRequest;
import com.ignou.smartmedical.dto.request.RegisterPatientRequest;
import com.ignou.smartmedical.dto.response.JwtResponse;
import com.ignou.smartmedical.dto.response.PatientResponse;
import com.ignou.smartmedical.entity.Admin;
import com.ignou.smartmedical.entity.Doctor;
import com.ignou.smartmedical.entity.Patient;
import com.ignou.smartmedical.enums.Role;
import com.ignou.smartmedical.exception.ResourceNotFoundException;
import com.ignou.smartmedical.repository.AdminRepository;
import com.ignou.smartmedical.repository.DoctorRepository;
import com.ignou.smartmedical.repository.PatientRepository;
import com.ignou.smartmedical.util.JwtUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    private boolean verifyPassword(String rawPassword, String storedPassword) {
        if (storedPassword == null) return false;
        if (storedPassword.startsWith("$2a$") || storedPassword.startsWith("$2b$") || storedPassword.startsWith("$2y$")) {
            return passwordEncoder.matches(rawPassword, storedPassword);
        }
        return rawPassword.equals(storedPassword);
    }

    @Transactional
    public PatientResponse registerPatient(RegisterPatientRequest req) {
        if (patientRepository.existsByEmail(req.getEmail())) {
            throw new IllegalArgumentException("Email is already registered: " + req.getEmail());
        }

        Patient patient = new Patient();
        patient.setName(req.getName());
        patient.setEmail(req.getEmail());
        patient.setPassword(passwordEncoder.encode(req.getPassword()));
        patient.setMobile(req.getMobile());
        patient.setAge(req.getAge());
        patient.setGender(req.getGender());
        patient.setAddress(req.getAddress());
        patient.setBloodGroup(req.getBloodGroup());
        patient.setMedicalHistory(req.getMedicalHistory());

        Patient saved = patientRepository.save(patient);

        return new PatientResponse(
                saved.getPatientId(),
                saved.getName(),
                saved.getEmail(),
                saved.getMobile(),
                saved.getAge(),
                saved.getGender(),
                saved.getAddress(),
                saved.getBloodGroup(),
                saved.getMedicalHistory(),
                saved.getCreatedAt()
        );
    }

    public JwtResponse login(LoginRequest req) {
        String input = req.getUsernameOrEmail().trim();
        String rawPassword = req.getPassword();
        Role role = req.getRole();

        if (role == Role.ADMIN) {
            Admin admin = adminRepository.findByUsername(input)
                    .or(() -> adminRepository.findByEmail(input))
                    .orElseThrow(() -> new BadCredentialsException("Invalid Admin username/email or password"));

            if (!verifyPassword(rawPassword, admin.getPassword())) {
                throw new BadCredentialsException("Invalid Admin username/email or password");
            }

            String token = jwtUtil.generateToken(admin.getAdminId(), admin.getUsername(), Role.ADMIN, admin.getUsername());
            return new JwtResponse(token, admin.getAdminId(), admin.getUsername(), Role.ADMIN, admin.getUsername());

        } else if (role == Role.DOCTOR) {
            String emailToSearch = input;
            if ("share@smartmed.com".equalsIgnoreCase(emailToSearch) || "share".equalsIgnoreCase(emailToSearch)) {
                emailToSearch = "sharma@smartmed.com";
            } else if (!emailToSearch.contains("@")) {
                emailToSearch = emailToSearch + "@smartmed.com";
            }

            final String lookupEmail = emailToSearch;
            Doctor doctor = doctorRepository.findByEmailIgnoreCase(input)
                    .or(() -> doctorRepository.findByEmailIgnoreCase(lookupEmail))
                    .or(() -> doctorRepository.findByEmail(input))
                    .or(() -> doctorRepository.findByEmail(lookupEmail))
                    .orElseThrow(() -> new BadCredentialsException("Invalid Doctor email or password"));

            if (!verifyPassword(rawPassword, doctor.getPassword())) {
                throw new BadCredentialsException("Invalid Doctor email or password");
            }

            String token = jwtUtil.generateToken(doctor.getDoctorId(), doctor.getEmail(), Role.DOCTOR, doctor.getName());
            return new JwtResponse(token, doctor.getDoctorId(), doctor.getEmail(), Role.DOCTOR, doctor.getName());

        } else if (role == Role.PATIENT) {
            Patient patient = patientRepository.findByEmail(input)
                    .orElseThrow(() -> new BadCredentialsException("Invalid Patient email or password"));

            if (!verifyPassword(rawPassword, patient.getPassword())) {
                throw new BadCredentialsException("Invalid Patient email or password");
            }

            String token = jwtUtil.generateToken(patient.getPatientId(), patient.getEmail(), Role.PATIENT, patient.getName());
            return new JwtResponse(token, patient.getPatientId(), patient.getEmail(), Role.PATIENT, patient.getName());
        }

        throw new BadCredentialsException("Unsupported user role");
    }
}
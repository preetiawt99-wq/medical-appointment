package com.ignou.smartmedical.service;

// Implements: Patient Management Module — synopsis section 9.2

import com.ignou.smartmedical.dto.request.PatientUpdateRequest;
import com.ignou.smartmedical.dto.response.PatientResponse;
import com.ignou.smartmedical.entity.Patient;
import com.ignou.smartmedical.exception.ResourceNotFoundException;
import com.ignou.smartmedical.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    public PatientResponse getPatientProfile(Long patientId) {
        Patient p = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));
        return mapToResponse(p);
    }

    @Transactional
    public PatientResponse updatePatientProfile(Long patientId, PatientUpdateRequest req) {
        Patient p = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        p.setName(req.getName());
        p.setMobile(req.getMobile());
        p.setAge(req.getAge());
        p.setGender(req.getGender());
        p.setAddress(req.getAddress());
        p.setBloodGroup(req.getBloodGroup());
        p.setMedicalHistory(req.getMedicalHistory());

        Patient updated = patientRepository.save(p);
        return mapToResponse(updated);
    }

    public List<PatientResponse> getAllPatients() {
        return patientRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePatient(Long patientId) {
        if (!patientRepository.existsById(patientId)) {
            throw new ResourceNotFoundException("Patient not found with id: " + patientId);
        }
        patientRepository.deleteById(patientId);
    }

    public PatientResponse mapToResponse(Patient p) {
        return new PatientResponse(
                p.getPatientId(),
                p.getName(),
                p.getEmail(),
                p.getMobile(),
                p.getAge(),
                p.getGender(),
                p.getAddress(),
                p.getBloodGroup(),
                p.getMedicalHistory(),
                p.getCreatedAt()
        );
    }
}
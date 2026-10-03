package com.ignou.smartmedical.service;

// Implements: Risk Prediction Module — synopsis section 9.5 & 11

import com.ignou.smartmedical.dto.request.RiskCheckRequest;
import com.ignou.smartmedical.dto.response.RiskReportResponse;
import com.ignou.smartmedical.entity.Appointment;
import com.ignou.smartmedical.entity.Patient;
import com.ignou.smartmedical.entity.RiskReport;
import com.ignou.smartmedical.enums.RiskLevel;
import com.ignou.smartmedical.exception.ResourceNotFoundException;
import com.ignou.smartmedical.repository.AppointmentRepository;
import com.ignou.smartmedical.repository.PatientRepository;
import com.ignou.smartmedical.repository.RiskReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class RiskPredictionService {

    @Autowired
    private RiskReportRepository riskReportRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    // Weight table matching synopsis requirements
    public static final int WEIGHT_LOW = 1;
    public static final int WEIGHT_MODERATE = 2;
    public static final int WEIGHT_HIGH = 4;

    public static final Map<String, Integer> SYMPTOM_WEIGHTS;
    static {
        Map<String, Integer> map = new HashMap<>();
        // Low severity symptoms (+1)
        map.put("FEVER", WEIGHT_LOW);
        map.put("HEADACHE", WEIGHT_LOW);
        map.put("FATIGUE", WEIGHT_LOW);
        map.put("JOINT_PAIN", WEIGHT_LOW);

        // Moderate severity symptoms (+2)
        map.put("HIGH_BLOOD_PRESSURE", WEIGHT_MODERATE);
        map.put("HIGH_BLOOD_SUGAR", WEIGHT_MODERATE);
        map.put("PROLONGED_COUGH", WEIGHT_MODERATE);
        map.put("DIZZINESS", WEIGHT_MODERATE);

        // High severity symptoms (+4)
        map.put("CHEST_PAIN", WEIGHT_HIGH);
        map.put("BREATHLESSNESS", WEIGHT_HIGH);

        SYMPTOM_WEIGHTS = Collections.unmodifiableMap(map);
    }

    public static final String MEDICAL_DISCLAIMER =
            " This is a preliminary indicator only and does not replace professional medical diagnosis.";

    public int calculateScore(int age, List<String> symptoms) {
        int score = 0;

        // 1. Symptom points
        if (symptoms != null) {
            for (String s : symptoms) {
                if (s != null) {
                    String key = s.trim().toUpperCase();
                    score += SYMPTOM_WEIGHTS.getOrDefault(key, 0);
                }
            }
        }

        // 2. Age factor: age >= 60 (+2), age 40-59 (+1), age < 40 (+0)
        if (age >= 60) {
            score += 2;
        } else if (age >= 40) {
            score += 1;
        }

        return score;
    }

    public RiskLevel determineRiskLevel(int totalScore) {
        if (totalScore <= 3) {
            return RiskLevel.LOW;
        } else if (totalScore <= 7) {
            return RiskLevel.MEDIUM;
        } else {
            return RiskLevel.HIGH;
        }
    }

    public String generateResultExplanation(RiskLevel level) {
        switch (level) {
            case HIGH:
                return "High risk detected based on acute symptoms reported. Immediate consultation with a doctor or specialist is strongly recommended." + MEDICAL_DISCLAIMER;
            case MEDIUM:
                return "Moderate risk detected based on reported symptoms and age. Please consider consulting a doctor soon." + MEDICAL_DISCLAIMER;
            case LOW:
            default:
                return "Low risk indicated based on current symptoms and age profile. Maintain healthy lifestyle and monitor symptoms." + MEDICAL_DISCLAIMER;
        }
    }

    @Transactional
    public RiskReportResponse evaluateRisk(Long patientId, RiskCheckRequest req) {
        Patient patient = patientRepository.findById(patientId)
                .orElseThrow(() -> new ResourceNotFoundException("Patient not found with id: " + patientId));

        Appointment appointment = null;
        if (req.getAppointmentId() != null) {
            appointment = appointmentRepository.findById(req.getAppointmentId()).orElse(null);
        }

        int score = calculateScore(req.getAge(), req.getSymptoms());
        RiskLevel level = determineRiskLevel(score);
        String explanation = generateResultExplanation(level);

        String symptomsString = String.join(", ", req.getSymptoms());

        RiskReport report = new RiskReport();
        report.setPatient(patient);
        report.setAppointment(appointment);
        report.setSymptoms(symptomsString);
        report.setRiskLevel(level);
        report.setResult(explanation);
        report.setReportDate(LocalDate.now());

        RiskReport saved = riskReportRepository.save(report);
        return mapToResponse(saved);
    }

    public List<RiskReportResponse> getPatientReports(Long patientId) {
        return riskReportRepository.findByPatientPatientIdOrderByReportDateDesc(patientId)
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<RiskReportResponse> getAllReports() {
        return riskReportRepository.findAllByOrderByReportDateDesc()
                .stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public RiskReportResponse mapToResponse(RiskReport r) {
        RiskReportResponse res = new RiskReportResponse();
        res.setReportId(r.getReportId());
        res.setPatientId(r.getPatient().getPatientId());
        res.setPatientName(r.getPatient().getName());
        res.setAppointmentId(r.getAppointment() != null ? r.getAppointment().getAppointmentId() : null);
        res.setSymptoms(r.getSymptoms());
        res.setRiskLevel(r.getRiskLevel());
        res.setResult(r.getResult());
        res.setReportDate(r.getReportDate());
        return res;
    }
}
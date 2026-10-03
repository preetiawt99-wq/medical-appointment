package com.ignou.smartmedical.service;

import com.ignou.smartmedical.dto.request.RiskCheckRequest;
import com.ignou.smartmedical.dto.response.RiskReportResponse;
import com.ignou.smartmedical.entity.Patient;
import com.ignou.smartmedical.entity.RiskReport;
import com.ignou.smartmedical.enums.RiskLevel;
import com.ignou.smartmedical.repository.AppointmentRepository;
import com.ignou.smartmedical.repository.PatientRepository;
import com.ignou.smartmedical.repository.RiskReportRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class RiskPredictionServiceTest {

    @Mock
    private RiskReportRepository riskReportRepository;

    @Mock
    private PatientRepository patientRepository;

    @Mock
    private AppointmentRepository appointmentRepository;

    @InjectMocks
    private RiskPredictionService riskPredictionService;

    private Patient testPatient;

    @BeforeEach
    void setUp() {
        testPatient = new Patient();
        testPatient.setPatientId(1L);
        testPatient.setName("Test Patient");
        testPatient.setAge(25);
    }

    @Test
    @DisplayName("Should score low risk for minor symptoms and young age (<40)")
    void testLowRiskScoring() {
        // Fever (+1) + Headache (+1) + Age 25 (+0) = Total 2 -> LOW
        int score = riskPredictionService.calculateScore(25, Arrays.asList("FEVER", "HEADACHE"));
        assertEquals(2, score);

        RiskLevel level = riskPredictionService.determineRiskLevel(score);
        assertEquals(RiskLevel.LOW, level);
    }

    @Test
    @DisplayName("Should score medium risk with moderate symptoms and middle age (40-59)")
    void testMediumRiskScoring() {
        // High BP (+2) + Prolonged Cough (+2) + Age 45 (+1) = Total 5 -> MEDIUM
        int score = riskPredictionService.calculateScore(45, Arrays.asList("HIGH_BLOOD_PRESSURE", "PROLONGED_COUGH"));
        assertEquals(5, score);

        RiskLevel level = riskPredictionService.determineRiskLevel(score);
        assertEquals(RiskLevel.MEDIUM, level);
    }

    @Test
    @DisplayName("Should score high risk for chest pain and breathlessness with elderly age (>=60)")
    void testHighRiskScoring() {
        // Chest Pain (+4) + Breathlessness (+4) + Age 65 (+2) = Total 10 -> HIGH
        int score = riskPredictionService.calculateScore(65, Arrays.asList("CHEST_PAIN", "BREATHLESSNESS"));
        assertEquals(10, score);

        RiskLevel level = riskPredictionService.determineRiskLevel(score);
        assertEquals(RiskLevel.HIGH, level);
    }

    @Test
    @DisplayName("TC-04: Risk Prediction should analyze symptoms and return risk result with preliminary indicator")
    void testEvaluateRiskAndSave() {
        RiskCheckRequest req = new RiskCheckRequest();
        req.setAge(28);
        req.setSymptoms(Collections.singletonList("FEVER"));

        when(patientRepository.findById(1L)).thenReturn(Optional.of(testPatient));
        when(riskReportRepository.save(any(RiskReport.class))).thenAnswer(invocation -> {
            RiskReport r = invocation.getArgument(0);
            r.setReportId(101L);
            return r;
        });

        RiskReportResponse response = riskPredictionService.evaluateRisk(1L, req);

        assertNotNull(response);
        assertEquals(RiskLevel.LOW, response.getRiskLevel());
        assertTrue(response.getResult().contains("preliminary indicator only"));
    }
}
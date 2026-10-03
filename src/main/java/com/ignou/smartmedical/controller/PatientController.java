package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.dto.request.PatientUpdateRequest;
import com.ignou.smartmedical.dto.response.AppointmentResponse;
import com.ignou.smartmedical.dto.response.PatientResponse;
import com.ignou.smartmedical.dto.response.RiskReportResponse;
import com.ignou.smartmedical.service.AppointmentService;
import com.ignou.smartmedical.service.PatientService;
import com.ignou.smartmedical.service.RiskPredictionService;
import com.ignou.smartmedical.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private RiskPredictionService riskPredictionService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractId(token);
    }

    @GetMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        Long patientId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(patientService.getPatientProfile(patientId));
    }

    @PutMapping("/me")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<PatientResponse> updateMyProfile(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody PatientUpdateRequest req) {
        Long patientId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(patientService.updatePatientProfile(patientId, req));
    }

    @GetMapping("/me/appointments")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        Long patientId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(appointmentService.getPatientAppointments(patientId));
    }

    @GetMapping("/me/risk-reports")
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<List<RiskReportResponse>> getMyRiskReports(@RequestHeader("Authorization") String authHeader) {
        Long patientId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(riskPredictionService.getPatientReports(patientId));
    }
}
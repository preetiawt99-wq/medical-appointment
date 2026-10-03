package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.dto.request.RiskCheckRequest;
import com.ignou.smartmedical.dto.response.RiskReportResponse;
import com.ignou.smartmedical.service.RiskPredictionService;
import com.ignou.smartmedical.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/risk-predictions")
public class RiskPredictionController {

    @Autowired
    private RiskPredictionService riskPredictionService;

    @Autowired
    private JwtUtil jwtUtil;

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<RiskReportResponse> checkRisk(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RiskCheckRequest req) {
        String token = authHeader.substring(7);
        Long patientId = jwtUtil.extractId(token);

        RiskReportResponse res = riskPredictionService.evaluateRisk(patientId, req);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }
}
package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.dto.request.LoginRequest;
import com.ignou.smartmedical.dto.request.RegisterPatientRequest;
import com.ignou.smartmedical.dto.response.JwtResponse;
import com.ignou.smartmedical.dto.response.PatientResponse;
import com.ignou.smartmedical.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private AuthService authService;

    @PostMapping("/register/patient")
    public ResponseEntity<PatientResponse> registerPatient(@Valid @RequestBody RegisterPatientRequest req) {
        PatientResponse res = authService.registerPatient(req);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<JwtResponse> login(@Valid @RequestBody LoginRequest req) {
        JwtResponse res = authService.login(req);
        return ResponseEntity.ok(res);
    }
}
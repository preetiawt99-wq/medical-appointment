package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.dto.request.AppointmentStatusUpdateRequest;
import com.ignou.smartmedical.dto.request.BookAppointmentRequest;
import com.ignou.smartmedical.dto.request.RescheduleAppointmentRequest;
import com.ignou.smartmedical.dto.response.AppointmentResponse;
import com.ignou.smartmedical.service.AppointmentService;
import com.ignou.smartmedical.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/appointments")
public class AppointmentController {

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentUserId(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractId(token);
    }

    private String getCurrentRole(String authHeader) {
        String token = authHeader.substring(7);
        return jwtUtil.extractRole(token);
    }

    @PostMapping
    @PreAuthorize("hasRole('PATIENT')")
    public ResponseEntity<AppointmentResponse> bookAppointment(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody BookAppointmentRequest req) {
        Long patientId = getCurrentUserId(authHeader);
        AppointmentResponse res = appointmentService.bookAppointment(patientId, req);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/{id}/reschedule")
    @PreAuthorize("hasAnyRole('PATIENT', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> rescheduleAppointment(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody RescheduleAppointmentRequest req) {
        Long userId = getCurrentUserId(authHeader);
        String role = getCurrentRole(authHeader);
        boolean isPatient = "PATIENT".equalsIgnoreCase(role);

        AppointmentResponse res = appointmentService.rescheduleAppointment(id, userId, isPatient, req);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/cancel")
    @PreAuthorize("hasAnyRole('PATIENT', 'DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> cancelAppointment(
            @PathVariable("id") Long id,
            @RequestHeader("Authorization") String authHeader) {
        Long userId = getCurrentUserId(authHeader);
        String role = getCurrentRole(authHeader);
        boolean isPatient = "PATIENT".equalsIgnoreCase(role);

        AppointmentResponse res = appointmentService.cancelAppointment(id, userId, isPatient);
        return ResponseEntity.ok(res);
    }

    @PutMapping("/{id}/status")
    @PreAuthorize("hasAnyRole('DOCTOR', 'ADMIN')")
    public ResponseEntity<AppointmentResponse> updateStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentStatusUpdateRequest req) {
        AppointmentResponse res = appointmentService.updateStatus(id, req.getStatus(), req.getDoctorNotes());
        return ResponseEntity.ok(res);
    }
}
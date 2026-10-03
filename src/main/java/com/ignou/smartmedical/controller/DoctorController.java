package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.dto.request.SlotRequest;
import com.ignou.smartmedical.dto.response.ApiResponse;
import com.ignou.smartmedical.dto.response.AppointmentResponse;
import com.ignou.smartmedical.dto.response.DoctorResponse;
import com.ignou.smartmedical.dto.response.DoctorSlotResponse;
import com.ignou.smartmedical.service.AppointmentService;
import com.ignou.smartmedical.service.DoctorService;
import com.ignou.smartmedical.util.JwtUtil;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private JwtUtil jwtUtil;

    private Long getCurrentUserId(String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new org.springframework.security.access.AccessDeniedException("Missing or invalid authorization token");
        }
        String token = authHeader.substring(7);
        return jwtUtil.extractId(token);
    }

    // Public / Authenticated search
    @GetMapping
    public ResponseEntity<List<DoctorResponse>> getAllDoctors(
            @RequestParam(value = "specialization", required = false) String specialization) {
        return ResponseEntity.ok(doctorService.getAllDoctors(specialization));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DoctorResponse> getDoctorById(@PathVariable("id") Long id) {
        return ResponseEntity.ok(doctorService.getDoctorById(id));
    }

    @GetMapping("/{id}/slots")
    public ResponseEntity<List<DoctorSlotResponse>> getAvailableSlots(
            @PathVariable("id") Long id,
            @RequestParam(value = "date", required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date) {
        return ResponseEntity.ok(doctorService.getAvailableSlots(id, date));
    }

    // Doctor role-specific actions
    @GetMapping("/me")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorResponse> getMyProfile(@RequestHeader("Authorization") String authHeader) {
        Long doctorId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(doctorService.getDoctorById(doctorId));
    }

    @GetMapping("/me/appointments")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<AppointmentResponse>> getMyAppointments(@RequestHeader("Authorization") String authHeader) {
        Long doctorId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(appointmentService.getDoctorAppointments(doctorId));
    }

    @PostMapping("/me/slots")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<DoctorSlotResponse> addSlot(
            @RequestHeader("Authorization") String authHeader,
            @Valid @RequestBody SlotRequest req) {
        Long doctorId = getCurrentUserId(authHeader);
        DoctorSlotResponse res = doctorService.addSlot(doctorId, req);
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @GetMapping("/me/slots")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<List<DoctorSlotResponse>> getMySlots(@RequestHeader("Authorization") String authHeader) {
        Long doctorId = getCurrentUserId(authHeader);
        return ResponseEntity.ok(doctorService.getAllSlotsForDoctor(doctorId));
    }

    @DeleteMapping("/me/slots/{slotId}")
    @PreAuthorize("hasRole('DOCTOR')")
    public ResponseEntity<ApiResponse> deleteSlot(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable("slotId") Long slotId) {
        Long doctorId = getCurrentUserId(authHeader);
        doctorService.deleteSlot(doctorId, slotId);
        return ResponseEntity.ok(new ApiResponse(true, "Slot deleted successfully"));
    }
}
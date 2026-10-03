package com.ignou.smartmedical.controller;

import com.ignou.smartmedical.dto.request.AppointmentStatusUpdateRequest;
import com.ignou.smartmedical.dto.request.CreateDoctorRequest;
import com.ignou.smartmedical.dto.request.UpdateDoctorRequest;
import com.ignou.smartmedical.dto.response.*;
import com.ignou.smartmedical.entity.AuditLog;
import com.ignou.smartmedical.enums.AppointmentStatus;
import com.ignou.smartmedical.service.AppointmentService;
import com.ignou.smartmedical.service.AuditLogService;
import com.ignou.smartmedical.service.DatabaseBackupService;
import com.ignou.smartmedical.service.DoctorService;
import com.ignou.smartmedical.service.PatientService;
import com.ignou.smartmedical.service.ReportService;
import com.ignou.smartmedical.service.RiskPredictionService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;

import java.util.List;

@RestController
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    @Autowired
    private DoctorService doctorService;

    @Autowired
    private PatientService patientService;

    @Autowired
    private AppointmentService appointmentService;

    @Autowired
    private ReportService reportService;

    @Autowired
    private RiskPredictionService riskPredictionService;

    @Autowired
    private AuditLogService auditLogService;

    // Doctor Management
    @GetMapping("/doctors")
    public ResponseEntity<List<DoctorResponse>> getAllDoctors() {
        return ResponseEntity.ok(doctorService.getAllDoctors(null));
    }

    @PostMapping("/doctors")
    public ResponseEntity<DoctorResponse> createDoctor(@Valid @RequestBody CreateDoctorRequest req) {
        DoctorResponse res = doctorService.createDoctor(req);
        auditLogService.logAction(null, "CREATE_DOCTOR", "DOCTOR", "Created doctor: " + res.getName() + " (" + res.getSpecialization() + ")");
        return new ResponseEntity<>(res, HttpStatus.CREATED);
    }

    @PutMapping("/doctors/{id}")
    public ResponseEntity<DoctorResponse> updateDoctor(
            @PathVariable("id") Long id,
            @Valid @RequestBody UpdateDoctorRequest req) {
        DoctorResponse res = doctorService.updateDoctor(id, req);
        auditLogService.logAction(null, "UPDATE_DOCTOR", "DOCTOR", "Updated doctor #" + id + ": " + res.getName());
        return ResponseEntity.ok(res);
    }

    @DeleteMapping("/doctors/{id}")
    public ResponseEntity<ApiResponse> deleteDoctor(@PathVariable("id") Long id) {
        doctorService.deleteDoctor(id);
        auditLogService.logAction(null, "DELETE_DOCTOR", "DOCTOR", "Deleted doctor #" + id);
        return ResponseEntity.ok(new ApiResponse(true, "Doctor deleted successfully"));
    }

    // Patient Management
    @GetMapping("/patients")
    public ResponseEntity<List<PatientResponse>> getAllPatients() {
        return ResponseEntity.ok(patientService.getAllPatients());
    }

    @DeleteMapping("/patients/{id}")
    public ResponseEntity<ApiResponse> deletePatient(@PathVariable("id") Long id) {
        patientService.deletePatient(id);
        auditLogService.logAction(null, "DELETE_PATIENT", "PATIENT", "Deleted patient #" + id);
        return ResponseEntity.ok(new ApiResponse(true, "Patient deleted successfully"));
    }

    // Appointment Oversight with Pagination
    @GetMapping("/appointments")
    public ResponseEntity<Page<AppointmentResponse>> getAppointments(
            @RequestParam(value = "status", required = false) AppointmentStatus status,
            @RequestParam(value = "page", defaultValue = "0") int page,
            @RequestParam(value = "size", defaultValue = "10") int size) {
        Pageable pageable = PageRequest.of(page, size, Sort.by("appointmentDate").descending().and(Sort.by("appointmentTime").descending()));
        return ResponseEntity.ok(appointmentService.getAllAppointments(status, pageable));
    }

    @PutMapping("/appointments/{id}/status")
    public ResponseEntity<AppointmentResponse> updateAppointmentStatus(
            @PathVariable("id") Long id,
            @Valid @RequestBody AppointmentStatusUpdateRequest req) {
        return ResponseEntity.ok(appointmentService.updateStatus(id, req.getStatus()));
    }

    // Reports & Audit Logs
    @GetMapping("/reports/summary")
    public ResponseEntity<SummaryReportResponse> getSummaryReport() {
        return ResponseEntity.ok(reportService.getSummaryReport());
    }

    @GetMapping("/reports/risk")
    public ResponseEntity<List<RiskReportResponse>> getAllRiskReports() {
        return ResponseEntity.ok(riskPredictionService.getAllReports());
    }

    @Autowired
    private DatabaseBackupService databaseBackupService;

    @GetMapping("/audit-logs")
    public ResponseEntity<List<AuditLog>> getAuditLogs() {
        return ResponseEntity.ok(auditLogService.getRecentLogs());
    }

    @GetMapping("/database/backup")
    public ResponseEntity<String> exportDatabaseBackup() {
        auditLogService.logAction(null, "DATABASE_BACKUP", "DATABASE", "Exported database SQL backup");
        String sqlBackup = databaseBackupService.generateSqlBackup();
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"smartmedical_backup_" + LocalDate.now() + ".sql\"")
                .contentType(MediaType.TEXT_PLAIN)
                .body(sqlBackup);
    }
}
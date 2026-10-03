package com.ignou.smartmedical.service;

// Implements: Reporting Module — synopsis section 9.6

import com.ignou.smartmedical.dto.response.SummaryReportResponse;
import com.ignou.smartmedical.enums.AppointmentStatus;
import com.ignou.smartmedical.repository.AppointmentRepository;
import com.ignou.smartmedical.repository.DoctorRepository;
import com.ignou.smartmedical.repository.PatientRepository;
import com.ignou.smartmedical.repository.RiskReportRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class ReportService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RiskReportRepository riskReportRepository;

    public SummaryReportResponse getSummaryReport() {
        SummaryReportResponse summary = new SummaryReportResponse();
        summary.setTotalPatients(patientRepository.count());
        summary.setTotalDoctors(doctorRepository.count());
        summary.setTotalAppointments(appointmentRepository.count());
        summary.setPendingAppointments(appointmentRepository.countByStatus(AppointmentStatus.PENDING));
        summary.setConfirmedAppointments(appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED));
        summary.setCancelledAppointments(appointmentRepository.countByStatus(AppointmentStatus.CANCELLED));
        summary.setCompletedAppointments(appointmentRepository.countByStatus(AppointmentStatus.COMPLETED));
        summary.setTotalRiskReports(riskReportRepository.count());
        return summary;
    }
}
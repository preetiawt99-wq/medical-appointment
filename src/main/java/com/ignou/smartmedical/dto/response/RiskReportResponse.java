package com.ignou.smartmedical.dto.response;

import com.ignou.smartmedical.enums.RiskLevel;
import java.time.LocalDate;

public class RiskReportResponse {
    private Long reportId;
    private Long patientId;
    private String patientName;
    private Long appointmentId;
    private String symptoms;
    private RiskLevel riskLevel;
    private String result;
    private LocalDate reportDate;

    public RiskReportResponse() {}

    public Long getReportId() { return reportId; }
    public void setReportId(Long reportId) { this.reportId = reportId; }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getPatientName() { return patientName; }
    public void setPatientName(String patientName) { this.patientName = patientName; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }

    public String getSymptoms() { return symptoms; }
    public void setSymptoms(String symptoms) { this.symptoms = symptoms; }

    public RiskLevel getRiskLevel() { return riskLevel; }
    public void setRiskLevel(RiskLevel riskLevel) { this.riskLevel = riskLevel; }

    public String getResult() { return result; }
    public void setResult(String result) { this.result = result; }

    public LocalDate getReportDate() { return reportDate; }
    public void setReportDate(LocalDate reportDate) { this.reportDate = reportDate; }
}
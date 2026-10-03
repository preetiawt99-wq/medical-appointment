package com.ignou.smartmedical.dto.request;

import com.ignou.smartmedical.enums.AppointmentStatus;
import jakarta.validation.constraints.NotNull;

public class AppointmentStatusUpdateRequest {
    @NotNull(message = "Status is required")
    private AppointmentStatus status;

    private String doctorNotes;

    public AppointmentStatusUpdateRequest() {}

    public AppointmentStatusUpdateRequest(AppointmentStatus status) {
        this.status = status;
    }

    public AppointmentStatusUpdateRequest(AppointmentStatus status, String doctorNotes) {
        this.status = status;
        this.doctorNotes = doctorNotes;
    }

    public AppointmentStatus getStatus() { return status; }
    public void setStatus(AppointmentStatus status) { this.status = status; }

    public String getDoctorNotes() { return doctorNotes; }
    public void setDoctorNotes(String doctorNotes) { this.doctorNotes = doctorNotes; }
}
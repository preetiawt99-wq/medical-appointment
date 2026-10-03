package com.ignou.smartmedical.dto.request;

import jakarta.validation.constraints.NotNull;

public class BookAppointmentRequest {
    @NotNull(message = "Doctor ID is required")
    private Long doctorId;

    @NotNull(message = "Slot ID is required")
    private Long slotId;

    private String reason;

    public BookAppointmentRequest() {}

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}
package com.ignou.smartmedical.dto.request;

import jakarta.validation.constraints.NotNull;

public class RescheduleAppointmentRequest {

    @NotNull(message = "New slot ID is required")
    private Long newSlotId;

    private String reason;

    public RescheduleAppointmentRequest() {}

    public RescheduleAppointmentRequest(Long newSlotId, String reason) {
        this.newSlotId = newSlotId;
        this.reason = reason;
    }

    public Long getNewSlotId() { return newSlotId; }
    public void setNewSlotId(Long newSlotId) { this.newSlotId = newSlotId; }

    public String getReason() { return reason; }
    public void setReason(String reason) { this.reason = reason; }
}

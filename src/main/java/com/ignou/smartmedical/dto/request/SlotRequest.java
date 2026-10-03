package com.ignou.smartmedical.dto.request;

import jakarta.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.LocalTime;

public class SlotRequest {
    @NotNull(message = "Slot date is required")
    private LocalDate slotDate;

    @NotNull(message = "Slot time is required")
    private LocalTime slotTime;

    public SlotRequest() {}

    public SlotRequest(LocalDate slotDate, LocalTime slotTime) {
        this.slotDate = slotDate;
        this.slotTime = slotTime;
    }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }
}
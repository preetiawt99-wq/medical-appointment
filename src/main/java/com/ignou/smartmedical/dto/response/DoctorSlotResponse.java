package com.ignou.smartmedical.dto.response;

import java.time.LocalDate;
import java.time.LocalTime;

public class DoctorSlotResponse {
    private Long slotId;
    private Long doctorId;
    private String doctorName;
    private String specialization;
    private LocalDate slotDate;
    private LocalTime slotTime;
    private Boolean isBooked;

    public DoctorSlotResponse() {}

    public DoctorSlotResponse(Long slotId, Long doctorId, String doctorName, String specialization, LocalDate slotDate, LocalTime slotTime, Boolean isBooked) {
        this.slotId = slotId;
        this.doctorId = doctorId;
        this.doctorName = doctorName;
        this.specialization = specialization;
        this.slotDate = slotDate;
        this.slotTime = slotTime;
        this.isBooked = isBooked;
    }

    public Long getSlotId() { return slotId; }
    public void setSlotId(Long slotId) { this.slotId = slotId; }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getDoctorName() { return doctorName; }
    public void setDoctorName(String doctorName) { this.doctorName = doctorName; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public LocalDate getSlotDate() { return slotDate; }
    public void setSlotDate(LocalDate slotDate) { this.slotDate = slotDate; }

    public LocalTime getSlotTime() { return slotTime; }
    public void setSlotTime(LocalTime slotTime) { this.slotTime = slotTime; }

    public Boolean getIsBooked() { return isBooked; }
    public void setIsBooked(Boolean isBooked) { this.isBooked = isBooked; }
}
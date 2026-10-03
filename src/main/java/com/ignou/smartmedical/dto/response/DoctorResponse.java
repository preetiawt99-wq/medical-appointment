package com.ignou.smartmedical.dto.response;

import java.time.LocalDateTime;

public class DoctorResponse {
    private Long doctorId;
    private String name;
    private String specialization;
    private String email;
    private String phone;
    private String availability;
    private LocalDateTime createdAt;

    public DoctorResponse() {}

    public DoctorResponse(Long doctorId, String name, String specialization, String email, String phone, String availability, LocalDateTime createdAt) {
        this.doctorId = doctorId;
        this.name = name;
        this.specialization = specialization;
        this.email = email;
        this.phone = phone;
        this.availability = availability;
        this.createdAt = createdAt;
    }

    public Long getDoctorId() { return doctorId; }
    public void setDoctorId(Long doctorId) { this.doctorId = doctorId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
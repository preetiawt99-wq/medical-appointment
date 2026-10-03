package com.ignou.smartmedical.dto.request;

import jakarta.validation.constraints.NotBlank;

public class UpdateDoctorRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @NotBlank(message = "Specialization is required")
    private String specialization;

    private String phone;
    private String availability;
    private String password; // optional

    public UpdateDoctorRequest() {}

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getSpecialization() { return specialization; }
    public void setSpecialization(String specialization) { this.specialization = specialization; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public String getAvailability() { return availability; }
    public void setAvailability(String availability) { this.availability = availability; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
}
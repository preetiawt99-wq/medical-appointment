package com.ignou.smartmedical.dto.request;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import java.util.List;

public class RiskCheckRequest {
    @NotNull(message = "Age is required")
    private Integer age;

    @NotEmpty(message = "At least one symptom must be specified")
    private List<String> symptoms;

    private Long appointmentId;

    public RiskCheckRequest() {}

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public List<String> getSymptoms() { return symptoms; }
    public void setSymptoms(List<String> symptoms) { this.symptoms = symptoms; }

    public Long getAppointmentId() { return appointmentId; }
    public void setAppointmentId(Long appointmentId) { this.appointmentId = appointmentId; }
}
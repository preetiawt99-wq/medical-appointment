package com.ignou.smartmedical.dto.response;

import java.time.LocalDateTime;

public class PatientResponse {
    private Long patientId;
    private String name;
    private String email;
    private String mobile;
    private Integer age;
    private String gender;
    private String address;
    private String bloodGroup;
    private String medicalHistory;
    private LocalDateTime createdAt;

    public PatientResponse() {}

    public PatientResponse(Long patientId, String name, String email, String mobile, Integer age, String gender, String address, String bloodGroup, String medicalHistory, LocalDateTime createdAt) {
        this.patientId = patientId;
        this.name = name;
        this.email = email;
        this.mobile = mobile;
        this.age = age;
        this.gender = gender;
        this.address = address;
        this.bloodGroup = bloodGroup;
        this.medicalHistory = medicalHistory;
        this.createdAt = createdAt;
    }

    public Long getPatientId() { return patientId; }
    public void setPatientId(Long patientId) { this.patientId = patientId; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getMobile() { return mobile; }
    public void setMobile(String mobile) { this.mobile = mobile; }

    public Integer getAge() { return age; }
    public void setAge(Integer age) { this.age = age; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public String getBloodGroup() { return bloodGroup; }
    public void setBloodGroup(String bloodGroup) { this.bloodGroup = bloodGroup; }

    public String getMedicalHistory() { return medicalHistory; }
    public void setMedicalHistory(String medicalHistory) { this.medicalHistory = medicalHistory; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}
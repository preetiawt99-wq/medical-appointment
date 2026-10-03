package com.ignou.smartmedical.service;

import com.ignou.smartmedical.entity.*;
import com.ignou.smartmedical.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

@Service
public class DatabaseBackupService {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private DoctorSlotRepository doctorSlotRepository;

    @Autowired
    private AppointmentRepository appointmentRepository;

    @Autowired
    private RiskReportRepository riskReportRepository;

    public String generateSqlBackup() {
        StringBuilder sb = new StringBuilder();
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));

        sb.append("-- =========================================================================\n");
        sb.append("-- IGNOU BCA Final Year Project: BCSP-064\n");
        sb.append("-- Smart Medical Appointment & Risk Prediction System\n");
        sb.append("-- Database Backup Export (Generated on: ").append(timestamp).append(")\n");
        sb.append("-- Student: Preeti (Enrollment No: 2401048429) | Guide: Gaurav Dhyani\n");
        sb.append("-- =========================================================================\n\n");

        sb.append("SET FOREIGN_KEY_CHECKS = 0;\n\n");

        // 1. Admin Table
        sb.append("-- Table: admin\n");
        List<Admin> admins = adminRepository.findAll();
        for (Admin a : admins) {
            sb.append(String.format("INSERT INTO `admin` (admin_id, username, password, email, created_at) VALUES (%d, '%s', '%s', '%s', '%s');\n",
                    a.getAdminId(), escape(a.getUsername()), escape(a.getPassword()), escape(a.getEmail()), a.getCreatedAt()));
        }
        sb.append("\n");

        // 2. Doctor Table
        sb.append("-- Table: doctor\n");
        List<Doctor> doctors = doctorRepository.findAll();
        for (Doctor d : doctors) {
            sb.append(String.format("INSERT INTO doctor (doctor_id, name, specialization, email, phone, password, availability, created_at) VALUES (%d, '%s', '%s', '%s', '%s', '%s', '%s', '%s');\n",
                    d.getDoctorId(), escape(d.getName()), escape(d.getSpecialization()), escape(d.getEmail()),
                    escape(d.getPhone()), escape(d.getPassword()), escape(d.getAvailability()), d.getCreatedAt()));
        }
        sb.append("\n");

        // 3. Patient Table
        sb.append("-- Table: patient\n");
        List<Patient> patients = patientRepository.findAll();
        for (Patient p : patients) {
            sb.append(String.format("INSERT INTO patient (patient_id, name, email, password, mobile, age, gender, address, blood_group, medical_history, created_at) VALUES (%d, '%s', '%s', '%s', '%s', %d, '%s', '%s', '%s', '%s', '%s');\n",
                    p.getPatientId(), escape(p.getName()), escape(p.getEmail()), escape(p.getPassword()),
                    escape(p.getMobile()), p.getAge() != null ? p.getAge() : 0, escape(p.getGender()),
                    escape(p.getAddress()), escape(p.getBloodGroup()), escape(p.getMedicalHistory()), p.getCreatedAt()));
        }
        sb.append("\n");

        // 4. Doctor Slot Table
        sb.append("-- Table: doctor_slot\n");
        List<DoctorSlot> slots = doctorSlotRepository.findAll();
        for (DoctorSlot s : slots) {
            sb.append(String.format("INSERT INTO doctor_slot (slot_id, doctor_id, slot_date, slot_time, is_booked) VALUES (%d, %d, '%s', '%s', %s);\n",
                    s.getSlotId(), s.getDoctor().getDoctorId(), s.getSlotDate(), s.getSlotTime(), s.getIsBooked() ? "TRUE" : "FALSE"));
        }
        sb.append("\n");

        // 5. Appointment Table
        sb.append("-- Table: appointment\n");
        List<Appointment> appointments = appointmentRepository.findAll();
        for (Appointment app : appointments) {
            Long slotId = app.getSlot() != null ? app.getSlot().getSlotId() : null;
            String slotVal = slotId != null ? slotId.toString() : "NULL";
            sb.append(String.format("INSERT INTO appointment (appointment_id, patient_id, doctor_id, slot_id, appointment_date, appointment_time, reason, doctor_notes, status, created_at) VALUES (%d, %d, %d, %s, '%s', '%s', '%s', '%s', '%s', '%s');\n",
                    app.getAppointmentId(), app.getPatient().getPatientId(), app.getDoctor().getDoctorId(), slotVal,
                    app.getAppointmentDate(), app.getAppointmentTime(), escape(app.getReason()), escape(app.getDoctorNotes()),
                    app.getStatus().name(), app.getCreatedAt()));
        }
        sb.append("\n");

        // 6. Risk Report Table
        sb.append("-- Table: risk_report\n");
        List<RiskReport> reports = riskReportRepository.findAll();
        for (RiskReport r : reports) {
            Long appId = r.getAppointment() != null ? r.getAppointment().getAppointmentId() : null;
            String appVal = appId != null ? appId.toString() : "NULL";
            sb.append(String.format("INSERT INTO risk_report (report_id, patient_id, appointment_id, symptoms, risk_level, result, report_date) VALUES (%d, %d, %s, '%s', '%s', '%s', '%s');\n",
                    r.getReportId(), r.getPatient().getPatientId(), appVal, escape(r.getSymptoms()),
                    escape(r.getRiskLevel() != null ? r.getRiskLevel().name() : ""), escape(r.getResult()), r.getReportDate()));
        }
        sb.append("\n");

        sb.append("SET FOREIGN_KEY_CHECKS = 1;\n");
        sb.append("-- End of Backup\n");

        return sb.toString();
    }

    private String escape(Object val) {
        if (val == null) return "";
        return val.toString().replace("'", "''").replace("\\", "\\\\");
    }
}

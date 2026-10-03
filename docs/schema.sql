-- =========================================================================
-- IGNOU BCA Final Year Project: BCSP-064
-- Title: Smart Medical Appointment and Risk Prediction System
-- Exported Relational Database DDL Schema (MySQL 8.x)
-- =========================================================================

CREATE DATABASE IF NOT EXISTS smartmedical_db;
USE smartmedical_db;

-- Drop tables in reverse dependency order if needed
DROP TABLE IF EXISTS audit_logs;
DROP TABLE IF EXISTS risk_report;
DROP TABLE IF EXISTS appointment;
DROP TABLE IF EXISTS doctor_slot;
DROP TABLE IF EXISTS doctor;
DROP TABLE IF EXISTS patient;
DROP TABLE IF EXISTS admin;

-- 1. Table: admin
CREATE TABLE admin (
    admin_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 2. Table: patient
CREATE TABLE patient (
    patient_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    mobile VARCHAR(15) NOT NULL,
    age INT NOT NULL,
    gender VARCHAR(10) NOT NULL,
    address VARCHAR(255),
    blood_group VARCHAR(10),
    medical_history TEXT,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 3. Table: doctor
CREATE TABLE doctor (
    doctor_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    specialization VARCHAR(100) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    phone VARCHAR(15),
    password VARCHAR(255) NOT NULL,
    availability VARCHAR(150),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 4. Table: doctor_slot
CREATE TABLE doctor_slot (
    slot_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    doctor_id BIGINT NOT NULL,
    slot_date DATE NOT NULL,
    slot_time TIME NOT NULL,
    is_booked BOOLEAN NOT NULL DEFAULT FALSE,
    CONSTRAINT fk_slot_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctor (doctor_id) ON DELETE CASCADE
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 5. Table: appointment
CREATE TABLE appointment (
    appointment_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    doctor_id BIGINT NOT NULL,
    slot_id BIGINT,
    appointment_date DATE NOT NULL,
    appointment_time TIME NOT NULL,
    reason VARCHAR(255),
    doctor_notes TEXT,
    status VARCHAR(20) NOT NULL DEFAULT 'PENDING',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_appointment_patient FOREIGN KEY (patient_id) 
        REFERENCES patient (patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_doctor FOREIGN KEY (doctor_id) 
        REFERENCES doctor (doctor_id) ON DELETE CASCADE,
    CONSTRAINT fk_appointment_slot FOREIGN KEY (slot_id) 
        REFERENCES doctor_slot (slot_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 6. Table: risk_report
CREATE TABLE risk_report (
    report_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    patient_id BIGINT NOT NULL,
    appointment_id BIGINT,
    symptoms TEXT NOT NULL,
    risk_level VARCHAR(20) NOT NULL,
    result VARCHAR(255) NOT NULL,
    report_date DATE NOT NULL,
    CONSTRAINT fk_risk_patient FOREIGN KEY (patient_id) 
        REFERENCES patient (patient_id) ON DELETE CASCADE,
    CONSTRAINT fk_risk_appointment FOREIGN KEY (appointment_id) 
        REFERENCES appointment (appointment_id) ON DELETE SET NULL
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 7. Table: audit_logs (Synopsis Chapter 7.3 & 12.5)
CREATE TABLE audit_logs (
    log_id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id BIGINT,
    action VARCHAR(100) NOT NULL,
    entity VARCHAR(100) NOT NULL,
    details VARCHAR(255),
    timestamp TIMESTAMP DEFAULT CURRENT_TIMESTAMP
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- Indexes for Query Optimization
CREATE INDEX idx_doctor_specialization ON doctor(specialization);
CREATE INDEX idx_slot_doctor_date ON doctor_slot(doctor_id, slot_date);
CREATE INDEX idx_appointment_patient ON appointment(patient_id);
CREATE INDEX idx_appointment_doctor ON appointment(doctor_id);
CREATE INDEX idx_appointment_status ON appointment(status);
CREATE INDEX idx_risk_patient ON risk_report(patient_id);
CREATE INDEX idx_audit_timestamp ON audit_logs(timestamp);
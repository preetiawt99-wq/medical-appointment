-- BCSP-064 Seed Data

-- 1. Admin Account (Default login: admin / Admin@123)
INSERT IGNORE INTO `admin` (admin_id, username, password, email, created_at)
VALUES (1, 'admin', 'Admin@123', 'admin@smartmed.com', NOW());

-- 2. Doctor Accounts (Default login password for all: Doctor@123)
INSERT IGNORE INTO doctor (doctor_id, name, specialization, email, phone, password, availability, created_at)
VALUES 
(1, 'Dr. Rajesh Sharma', 'Cardiology', 'sharma@smartmed.com', '+91 9876543210', 'Doctor@123', 'Mon-Fri 09:00 - 13:00', NOW()),
(2, 'Dr. Priya Patel', 'Neurology', 'patel@smartmed.com', '+91 9876543211', 'Doctor@123', 'Mon-Thu 10:00 - 15:00', NOW()),
(3, 'Dr. Amit Verma', 'Orthopedics', 'verma@smartmed.com', '+91 9876543212', 'Doctor@123', 'Tue-Sat 09:00 - 14:00', NOW()),
(4, 'Dr. Sunita Rao', 'Pediatrics', 'rao@smartmed.com', '+91 9876543213', 'Doctor@123', 'Mon-Fri 14:00 - 18:00', NOW()),
(5, 'Dr. Vikram Malhotra', 'General Medicine', 'malhotra@smartmed.com', '+91 9876543214', 'Doctor@123', 'Mon-Sat 08:00 - 16:00', NOW());

-- 3. Patient Account (Default login: preeti@example.com / Patient@123)
INSERT IGNORE INTO patient (patient_id, name, email, password, mobile, age, gender, address, blood_group, medical_history, created_at)
VALUES 
(1, 'Preeti', 'preeti@example.com', 'Patient@123', '+91 9123456780', 23, 'Female', 'New Delhi, India', 'O+', 'None / Mild seasonal allergies', NOW());

-- 4. Initial Available Doctor Slots for Immediate Demonstration
INSERT IGNORE INTO doctor_slot (slot_id, doctor_id, slot_date, slot_time, is_booked)
VALUES
-- Dr. Sharma (Cardiology) Slots
(1, 1, CURRENT_DATE, '09:30:00', FALSE),
(2, 1, CURRENT_DATE, '10:30:00', FALSE),
(3, 1, CURRENT_DATE, '11:30:00', FALSE),
(4, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '09:30:00', FALSE),
(5, 1, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:30:00', FALSE),
(6, 1, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '11:00:00', FALSE),

-- Dr. Patel (Neurology) Slots
(7, 2, CURRENT_DATE, '10:00:00', FALSE),
(8, 2, CURRENT_DATE, '11:00:00', FALSE),
(9, 2, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '10:00:00', FALSE),
(10, 2, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '14:00:00', FALSE),

-- Dr. Verma (Orthopedics) Slots
(11, 3, CURRENT_DATE, '09:00:00', FALSE),
(12, 3, CURRENT_DATE, '10:00:00', FALSE),
(13, 3, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '09:00:00', FALSE),
(14, 3, DATE_ADD(CURRENT_DATE, INTERVAL 2 DAY), '12:00:00', FALSE),

-- Dr. Rao (Pediatrics) Slots
(15, 4, CURRENT_DATE, '14:30:00', FALSE),
(16, 4, CURRENT_DATE, '15:30:00', FALSE),
(17, 4, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '14:30:00', FALSE),

-- Dr. Malhotra (General Medicine) Slots
(18, 5, CURRENT_DATE, '08:30:00', FALSE),
(19, 5, CURRENT_DATE, '09:30:00', FALSE),
(20, 5, DATE_ADD(CURRENT_DATE, INTERVAL 1 DAY), '08:30:00', FALSE);
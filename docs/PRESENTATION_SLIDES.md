# IGNOU BCA FINAL YEAR MAJOR PROJECT (BCSP-064)
## VIVA VOCE PRESENTATION SLIDES
### Smart Medical Appointment & Risk Prediction System

**Student:** Preeti  
**Enrollment Number:** 2401048429  
**Course Code:** BCSP-064  
**Project Guide:** Gaurav Dhyani (Cloud Administrator)  
**Study Centre / University:** Indira Gandhi National Open University (IGNOU), SOCIS  

---

## SLIDE 1: TITLE SLIDE
```text
========================================================================
       SMART MEDICAL APPOINTMENT & RISK PREDICTION SYSTEM
========================================================================
  A Full-Stack Enterprise Healthcare Management & Triage Platform

  Submitted for the award of Bachelor of Computer Applications (BCA)
  Course Code: BCSP-064

  Presented By: PREETI (Enrollment No: 2401048429)
  Under Guidance Of: GAURAV DHYANI (Cloud Administrator)
  Indira Gandhi National Open University (IGNOU)
========================================================================
```

---

## SLIDE 2: INTRODUCTION & BACKGROUND
- **Healthcare Dilemma:** Rapidly growing outpatient volumes put significant pressure on hospital reception desks and clinic scheduling.
- **Current Challenges:**
  - Traditional manual scheduling involves crowded waiting rooms, paper registers, and high error rates.
  - Lack of preliminary health risk awareness leads patients to neglect critical symptoms until acute stages.
- **The Solution:**
  - A secure web-based scheduling platform providing conflict-free slot booking.
  - An integrated **Rule-Based Risk Prediction Engine** that screens symptoms and connects high-risk patients directly to relevant medical specialists.

---

## SLIDE 3: PROBLEM STATEMENT & DRAWBACKS OF EXISTING SYSTEM
1. **Manual Appointment Process:** Phone calls and in-person visits cause long waiting times.
2. **Scheduling Conflicts & Double-Booking:** Paper registers or basic spreadsheets fail under concurrent booking attempts.
3. **Data Fragmentation:** Patient histories, past appointments, and prescriptions are scattered across physical files.
4. **No Early Triage:** Patients cannot evaluate symptom severity before booking.
5. **Inefficient Administration:** Manual report compilation and absence of tamper-proof audit trails.

---

## SLIDE 4: PROJECT OBJECTIVES
- **Online Booking & Rescheduling:** Enable patients to reserve, reschedule, or cancel appointments from home.
- **Concurrency Control:** Implement transactional database locks ensuring zero duplicate slot reservations.
- **Automated Health Risk Triage:** Screen 10 symptom parameters and classify urgency into **LOW**, **MEDIUM**, or **HIGH** risk.
- **Triage-to-Booking Continuity:** Pre-fill doctor consultation reasons automatically when elevated health risks are detected.
- **Clinical Documentation:** Provide doctors with patient clinical profiles (Blood Group, Medical History) and consultation notes/e-prescription tools.
- **Admin Analytics:** Deliver summary metrics, CSV report exports, and security audit logs.

---

## SLIDE 5: SYSTEM ARCHITECTURE & TECHNOLOGY STACK
- **Architectural Style:** 3-Tier Layered MVC with RESTful Web APIs.
- **Backend Tier:**
  - Java 17 LTS
  - Spring Boot 3.3.4 (Spring Web, Spring Security 6, Spring Data JPA)
  - Hibernate ORM with HikariCP connection pooling
- **Security:** Stateless JSON Web Tokens (JWT) signed with HMAC-SHA256 and BCrypt password encryption.
- **Data Tier:** MySQL 8.x (Production) and H2 in MySQL Mode (Development & Testing).
- **Presentation Tier:** HTML5, CSS3, JavaScript (ES6+ Fetch API), Bootstrap 5.3.3, Bootstrap Icons.

---

## SLIDE 6: MODULE BREAKDOWN (SYNOPSIS ALIGNED)
1. **Authentication & RBAC:** Admin, Doctor, and Patient login with JWT session control.
2. **Patient Management:** Self-registration, clinical profile updates, blood group & medical history tracking.
3. **Doctor Management:** Specialty catalog, schedule slot configuration, slot deletion, consultation notes.
4. **Appointment Management:** Real-time slot reservation, atomic rescheduling, cancellation, token slip printing.
5. **Risk Prediction:** 10-symptom multi-factor rule engine with medical disclaimer and direct consultation referral.
6. **Reporting & Exports:** Printable appointment slips, CSV exports for appointments, risk audits, and operational logs.
7. **Patient Portal:** Comprehensive dashboard with appointment tracking and risk assessment history.
8. **Admin Portal:** Hospital oversight, doctor onboarding/editing, patient directory, and audit logs.
9. **Database Management:** 3NF relational schema, cascading foreign keys, audit logging, and on-demand SQL backup export.

---

## SLIDE 7: DATA FLOW DIAGRAMS (DFD LEVELS 0, 1 & 2)
- **Level 0 (Context):** Primary entities (Patient, Doctor, Admin) interact with the central Smart Medical System.
- **Level 1:** Decomposes into 5 core processes:
  - 1.0 User Registration & Login
  - 2.0 Appointment Management
  - 3.0 Doctor Management
  - 4.0 Risk Prediction
  - 5.0 Reports & Audit Logging
- **Level 2 (Synopsis Detail):** 11 detailed sub-processes from Process 3.1 (Capture Patient Request) through Process 3.11 (View Appointment & Clinical Risk Details) interacting with data stores D1 (Patient), D2 (Doctor), D3 (Appointment), and D4 (Risk).

---

## SLIDE 8: DATABASE DESIGN & ENTITY RELATIONSHIPS
- **Normalized Schema (3NF):**
  - `admin`: System credentials and master privileges.
  - `patient`: Demographic and clinical data (`blood_group`, `medical_history`).
  - `doctor`: Specialty, credentials, contact, and consulting hours.
  - `doctor_slot`: Granular calendar slots with `is_booked` atomic lock flag.
  - `appointment`: Booking state, visit date/time, and doctor consultation notes.
  - `risk_report`: Symptom records, evaluated risk category, and clinical recommendations.
  - `audit_logs`: User identity, action type, affected entity, and timestamp.

---

## SLIDE 9: RULE-BASED RISK PREDICTION ALGORITHM
```text
           [Patient Input: Age + Selected Symptoms]
                             |
         +-------------------v-------------------+
         | Does input contain CHEST_PAIN or       | --- YES ---> [ HIGH RISK ]
         | BREATHLESSNESS (Acute Emergency)?     |              Immediate ER Care
         +---------------------------------------+
                             | NO
         +-------------------v-------------------+
         | Is Age > 50 AND symptoms contain      | --- YES ---> [ HIGH RISK ]
         | HIGH_BP and HIGH_SUGAR?               |              Urgent Specialist
         +---------------------------------------+
                             | NO
         +-------------------v-------------------+
         | Symptoms Count >= 3 OR               | --- YES ---> [ MEDIUM RISK ]
         | (PROLONGED_COUGH and FEVER)?          |              Medical Follow-up
         +---------------------------------------+
                             | NO
                             v
                     [ LOW RISK ]
                     Routine Outpatient Visit
```

---

## SLIDE 10: TRIAGE-TO-BOOKING CONTINUITY
1. **Assessment Result:** Patient receives evaluated risk level with transparent clinical explanations.
2. **Dynamic Redirection:** When `HIGH` or `MEDIUM` risk is identified, the system attaches evaluated symptoms and urgency as URL parameters.
3. **Form Auto-Population:** The appointment booking form captures parameters, automatically populates the consultation reason, and highlights an alert informing the patient of their triage referral.
4. **Doctor Insight:** The consulting doctor sees the patient's reported symptoms, risk category, blood group, and medical history upon patient arrival.

---

## SLIDE 11: VERIFICATION & TESTING (100% SUCCESS)
- **Synopsis Test Scenarios (TC-01 to TC-05):**
  - TC-01 (Valid Login) $\to$ **PASS** (JWT Token Issued)
  - TC-02 (Book Appointment) $\to$ **PASS** (Slot Locked, Appointment Created)
  - TC-03 (Invalid Login) $\to$ **PASS** (HTTP 401 Unauthorized with Error Message)
  - TC-04 (Risk Prediction) $\to$ **PASS** (High Risk Flagged with Emergency Note & Referral)
  - TC-05 (Unauthorized Access) $\to$ **PASS** (HTTP 403 Forbidden)
- **Automated Test Matrix:**
  - 21 automated unit & integration tests executed via Maven Surefire:
  - `AppointmentControllerTest` (4 tests) $\to$ **PASS**
  - `AuthControllerTest` (3 tests) $\to$ **PASS**
  - `AppointmentServiceTest` (5 tests) $\to$ **PASS**
  - `DoctorServiceTest` (5 tests) $\to$ **PASS**
  - `RiskPredictionServiceTest` (4 tests) $\to$ **PASS**
  - **Overall:** 21 Tests Run, 0 Failures, 0 Errors (100% Pass Rate).

---

## SLIDE 12: CONCLUSION & FUTURE SCOPE
- **Key Achievements:**
  - 100% compliance with approved IGNOU BCSP-064 project proposal.
  - Reliable concurrency locking preventing double-booking under load.
  - Transparent rule-based health triage improving patient care.
  - Production-ready documentation, schema, and API collections.
- **Future Enhancements:**
  - Automated SMS and WhatsApp appointment reminders.
  - Telemedicine WebRTC video consultations.
  - Machine Learning models (Random Forest) for multi-disease diagnosis.
  - Ayushman Bharat Digital Mission (ABDM) electronic health records integration.

---

## THANK YOU!
**Questions & Answers / Demonstration Session**
- **Candidate:** Preeti (Enrollment No: 2401048429)
- **Project:** Smart Medical Appointment & Risk Prediction System (BCSP-064)

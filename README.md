# Smart Medical Appointment and Risk Prediction System
**IGNOU BCA Final Year Project (Course Code: BCSP-064)**

---

## 1. Project Overview

The **Smart Medical Appointment and Risk Prediction System** is a full-stack healthcare web application developed for the Bachelor of Computer Applications (BCA) programme at Indira Gandhi National Open University (IGNOU).

The system addresses scheduling conflicts, long patient waiting times, lack of preliminary risk awareness, and operational traceability by providing:
1. **Multi-Role Portals**: Dedicated, secure interfaces for **Patients**, **Doctors**, and **Administrators**.
2. **Conflict-Free Scheduling**: Granular slot-level booking preventing double-booking and managing appointment lifecycles (`PENDING`, `CONFIRMED`, `COMPLETED`, `CANCELLED`).
3. **Explainable Health Risk Triage**: A transparent, rule-based screening engine evaluating symptoms and age into `LOW`, `MEDIUM`, and `HIGH` risk categories accompanied by formal medical disclaimers.
4. **Audit Logging & Analytics**: Operational traceability recording critical user and doctor actions (`audit_logs`), appointment metrics, and patient directories.

---

## 2. Technology Stack

* **Backend**: Java 17+ (LTS), Spring Boot 3.3.x (Spring Framework 6/7)
* **Security**: Spring Security 6/7, Stateless JWT Authentication, BCrypt Password Hashing
* **Database**: MySQL 8.x, Spring Data JPA / Hibernate ORM
* **Frontend**: HTML5, CSS3, JavaScript (ES6+ `fetch`), Bootstrap 5
* **Web Server**: Embedded Tomcat
* **Testing & API**: JUnit 5, Mockito, MockMvc, Postman

---

## 3. Prerequisites & Setup

### 3.1 Prerequisites
* **Java**: OpenJDK 17 or higher (`java -version`)
* **Database**: MySQL Server 8.x running on port `3306`
* **Build Tool**: Pre-configured Maven Wrapper included (`mvnw.cmd` on Windows, `./mvnw` on Linux/macOS — no global Maven install needed!)

### 3.2 Database Initialization
Open MySQL Workbench or MySQL CLI and run:

```sql
CREATE DATABASE IF NOT EXISTS smartmedical_db;
```

> **Note:** Hibernate automatically creates and updates tables on application launch (`spring.jpa.hibernate.ddl-auto=update`). Initial seed data (`data.sql`) populates accounts and open doctor slots automatically.

### 3.3 Configuration (application.yml)
Credentials can be overridden via environment variables or modified directly in `src/main/resources/application.yml`:

```yaml
spring:
  datasource:
    url: jdbc:mysql://${DB_HOST:localhost}:${DB_PORT:3306}/${DB_NAME:smartmedical_db}?createDatabaseIfNotExist=true&useSSL=false&allowPublicKeyRetrieval=true&serverTimezone=UTC
    username: ${DB_USER:root}
    password: ${DB_PASSWORD:root}
```

---

## 4. Running the Application

From the project root directory:

**On Windows:**
```cmd
.\mvnw.cmd spring-boot:run
```

**On Linux/macOS:**
```bash
./mvnw spring-boot:run
```

Once started, open your browser and navigate to:
👉 **`http://localhost:8080`**

### 4.1 Cloud Deployment (Render + Aiven MySQL)

The repository contains a `Dockerfile`, so it can be deployed as a Docker web service on [Render](https://render.com) with a managed MySQL database such as [Aiven for MySQL](https://aiven.io) (free plan).

1. On Render choose **New → Web Service**, connect this GitHub repository and keep the detected **Docker** runtime.
2. Add the following environment variables (values from the Aiven *Connection information* panel):

| Variable | Example value | Purpose |
| :--- | :--- | :--- |
| `DB_HOST` | `mysql-xxxx.aivencloud.com` | MySQL host |
| `DB_PORT` | `12345` | MySQL port |
| `DB_NAME` | `defaultdb` | Database name |
| `DB_USER` | `avnadmin` | Database user |
| `DB_PASSWORD` | *(secret)* | Database password |
| `DB_SSL_MODE` | `REQUIRED` | Encrypted connection (required by Aiven) |
| `JWT_SECRET` | *(random string, at least 32 characters)* | Signing key for login tokens |

`PORT` is supplied by Render automatically and is read by `server.port`. Locally none of these variables are needed: the defaults in `application.yml` connect to MySQL on `localhost:3306` without SSL.

**Keep-alive on free plans.** `GET /api/health` is a public endpoint that runs a database query and returns `{"status":"UP","database":"UP"}` (HTTP 503 when the database is unreachable). On Render the application calls its own public URL (`RENDER_EXTERNAL_URL`, set by Render) every 10 minutes, so the free web service does not sleep and the free database sees regular activity. Set `KEEPALIVE_URL` to override the URL or `KEEPALIVE_INTERVAL_MS` to change the interval; without a URL (local runs) the ping is disabled. An always-on free service uses about 744 of Render's 750 free instance hours per month, so keep it as the only free web service in the workspace.

---

## 5. Seeded Accounts for Testing & Viva Demo

The application comes pre-loaded with demonstration accounts via `data.sql`:

| Role | Username / Email | Password | Dashboard URL |
| :--- | :--- | :--- | :--- |
| **Administrator** | `admin` (or `admin@smartmed.com`) | `Admin@123` | `http://localhost:8080/admin/dashboard.html` |
| **Doctor** (Cardiology) | `sharma@smartmed.com` | `Doctor@123` | `http://localhost:8080/doctor/dashboard.html` |
| **Doctor** (Neurology) | `patel@smartmed.com` | `Doctor@123` | `http://localhost:8080/doctor/dashboard.html` |
| **Doctor** (Orthopedics) | `verma@smartmed.com` | `Doctor@123` | `http://localhost:8080/doctor/dashboard.html` |
| **Doctor** (Pediatrics) | `rao@smartmed.com` | `Doctor@123` | `http://localhost:8080/doctor/dashboard.html` |
| **Doctor** (General Med) | `malhotra@smartmed.com` | `Doctor@123` | `http://localhost:8080/doctor/dashboard.html` |
| **Patient** | `preeti@example.com` | `Patient@123` | `http://localhost:8080/patient/dashboard.html` |

*(New patients can also register directly through the public [Register Page](http://localhost:8080/register.html).)*

---

## 6. URL & Navigation Map

* **Public Portal**:
  * `http://localhost:8080/` or `/index.html` — Landing page with role selector login
  * `http://localhost:8080/register.html` — Patient registration
* **Patient Portal**:
  * `/patient/dashboard.html` — Overview of bookings, latest risk check, and personal health metrics
  * `/patient/book-appointment.html` — Doctor selection, real-time slot booking
  * `/patient/my-appointments.html` — Appointment history, cancellation, **slot rescheduling**, and **printable appointment slip/token**
  * `/patient/risk-check.html` — Interactive rule-based risk evaluation form
  * `/patient/profile.html` — Personal profile and **medical history & blood group management**
* **Doctor Portal**:
  * `/doctor/dashboard.html` — Practice statistics and today's schedule
  * `/doctor/appointments.html` — Review patients, inspect medical histories, confirm visits, and **record consultation prescriptions/notes** upon completion
  * `/doctor/availability.html` — Add and manage available consultation slots
* **Administrator Portal**:
  * `/admin/dashboard.html` — Platform metrics and recent appointments
  * `/admin/manage-doctors.html` — Provision doctors, edit doctor profiles, and update specialties
  * `/admin/manage-patients.html` — Patient account directory with medical history inspection
  * `/admin/manage-appointments.html` — Status filter, lifecycle oversight, and **CSV export**
  * `/admin/reports.html` — Aggregate summary, risk triage audit log, and **CSV report exports**

---

## 7. Running Tests & Postman Verification

### Run Unit & Integration Tests
```cmd
.\mvnw.cmd test
```

Includes:
* `RiskPredictionServiceTest`: Validates scoring boundaries (Low: 0-3, Medium: 4-7, High: 8+) and age factors.
* `AppointmentServiceTest`: Verifies slot locking (`is_booked = true`), audit trail logging, duplicate conflict rejection (`SlotUnavailableException`), slot rescheduling with old slot release, and doctor consultation notes.
* `AuthControllerTest`: MockMvc REST endpoint validation for admin login.
* `AppointmentControllerTest`: MockMvc REST endpoint validation for appointment booking, rescheduling, status updates, and cancellation.

### Postman Test Suite
Import `postman/SmartMedicalAppointment.postman_collection.json` into Postman to test all REST endpoints across Auth, Patient, Doctor, and Admin scopes.

---

## 8. IGNOU BCSP-064 Viva Notes

1. **Architecture**: 3-Tier Layered Architecture (Browser UI $\rightarrow$ Spring Boot Controllers $\rightarrow$ Business Services $\rightarrow$ Spring Data JPA Repositories $\rightarrow$ MySQL Database).
2. **Double Booking Prevention**: Doctor slots are stored in a dedicated `doctor_slot` table. When an appointment is booked or rescheduled, transactional locking marks `is_booked = true`. Any subsequent booking attempt throws `SlotUnavailableException`.
3. **Reschedule Mechanism**: Transactionally releases the prior slot (`is_booked = false`) and claims the new slot (`is_booked = true`), resetting the status to `PENDING` for doctor confirmation.
4. **Clinical Records**: Patients maintain Blood Group and Medical History (allergies, chronic conditions) which are visible to consulting doctors and printed on the official token slip.
5. **Risk Scoring Engine**: Implements a transparent, deterministic rule-based model (weighted points for 10 key symptoms + age brackets) mapped to Low, Medium, and High categories, with an explicit medical disclaimer.
6. **Audit Trail**: Recorded in `audit_logs` table tracking critical business actions (booking, cancellation, rescheduling, status updates, doctor provisioning).
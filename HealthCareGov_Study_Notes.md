# HealthCareGov — Complete Project Study Notes
### Public Healthcare & Hospital Management System

---

# TABLE OF CONTENTS

1. [Project Overview](#1-project-overview)
2. [Architecture Explanation](#2-architecture-explanation)
3. [Module-wise Breakdown](#3-module-wise-breakdown)
4. [Database Design](#4-database-design)
5. [Authentication & Authorization](#5-authentication--authorization)
6. [API Flow Explanation](#6-api-flow-explanation)
7. [Validation & Error Handling](#7-validation--error-handling)
8. [Key Design Decisions](#8-key-design-decisions)
9. [How to Run the Project](#9-how-to-run-the-project)

---

# 1. PROJECT OVERVIEW

## 1.1 Purpose

HealthCareGov is a **REST API backend** built with **Java Spring Boot** for managing public healthcare delivery across government hospitals. It is designed to serve multiple user types — patients, doctors, hospital administrators, program managers, compliance officers, and government auditors.

The system handles everything from patient registration and appointment booking to treatment recording, hospital resource tracking, compliance monitoring, and analytics reporting. It exposes a secured REST API that a frontend (Angular/React) would consume.

## 1.2 Problem It Solves

In a public healthcare environment, several challenges exist:
- No unified system for patients to register, book appointments, and track their own medical history.
- No central way for administrators to manage multiple hospitals, their resources (beds, equipment, staff), and workflows.
- No audit trail or compliance checking mechanism to ensure policies are followed.
- No governance-level visibility into program performance (appointment rates, treatment completion, compliance pass rates).

HealthCareGov addresses all of these by providing a **role-secured, audited, multi-module REST backend**.

## 1.3 Key Features (Implemented)

| Feature | Implemented By |
|---|---|
| JWT-based login and role-based access | Spring Security + JwtUtil |
| Patient self-registration (status: Pending) | PatientService |
| Admin approval of patients | UserService.updateStatus |
| Doctor schedule/slot management | ScheduleService |
| Patient appointment booking and cancellation | AppointmentService |
| Admin workflow (reassign, check-in) | AppointmentService |
| Doctor treatment recording | TreatmentService |
| Medical record update | TreatmentService |
| Hospital CRUD | HospitalService |
| Resource (Beds/Equipment/Staff) management | ResourceService |
| Analytics dashboard | AnalyticsService |
| Compliance record management | ComplianceService |
| Audit management (Scheduled → In-Progress → Completed) | AuditService |
| Audit logs (all actions tracked via AOP) | AuditAspect + LogService |
| Notifications (auto-created on key events) | NotificationService |
| Program dashboard and report generation | ProgramService |

---

# 2. ARCHITECTURE EXPLANATION

## 2.1 The Layered Architecture

The project follows a **classic 4-layer Spring Boot architecture**. Each layer has a single responsibility and communicates only with the layer directly adjacent to it.

```
HTTP Request
     │
     ▼
┌─────────────────────────────┐
│       CONTROLLER LAYER       │  ← Receives HTTP, validates input, calls Service
├─────────────────────────────┤
│        SERVICE LAYER         │  ← Business logic lives here
├─────────────────────────────┤
│      REPOSITORY (DAO) LAYER  │  ← Database access via Spring Data JPA
├─────────────────────────────┤
│          DATABASE            │  ← MySQL tables mapped to Entities
└─────────────────────────────┘
         ▲         │
         │         ▼
      Entity     DTO
   (DB model)  (API model)
```

### Layer 1 — Controller

**Package:** `com.cognizant.healthcaregov.controller`

Controllers are annotated with `@RestController`. They:
- Accept incoming HTTP requests at defined URL paths (`@RequestMapping`, `@GetMapping`, etc.)
- Extract data from the URL path (`@PathVariable`), query parameters (`@RequestParam`), and request body (`@RequestBody`)
- Delegate all business logic to the Service layer
- Return `ResponseEntity<T>` with an appropriate HTTP status code

**Example — PatientController.java:**
```java
@PostMapping("/register")
public ResponseEntity<PatientResponse> register(
        @Valid @RequestBody PatientRegisterRequest req) {
    return new ResponseEntity<>(patientService.register(req), HttpStatus.CREATED);
}
```

The controller does NO business logic. It just hands `req` to the service and wraps the result in a 201 Created response.

### Layer 2 — Service

**Package:** `com.cognizant.healthcaregov.service`

Services are annotated with `@Service`. They:
- Contain **all business logic** — validation rules, status transitions, data transformations
- Call one or more repositories to fetch or persist data
- Convert Entities to DTOs before returning
- Handle business rule violations by throwing custom exceptions (`BadRequestException`, `ResourceNotFoundException`)
- Are called by Controllers and by each other (e.g., `AppointmentService` calls `ScheduleService`)

**Example — PatientService.register():**
```java
// Checks for duplicate email
if (userRepository.findByEmail(req.getEmail()).isPresent()) {
    throw new BadRequestException("Email is already registered: " + req.getEmail());
}
// Creates User entity, saves it, then creates Patient entity linked to that User
User user = new User(); // ... set fields
User savedUser = userRepository.save(user);
Patient patient = new Patient(); // ... set fields, link to savedUser
Patient saved = patientRepository.save(patient);
return toResponse(saved); // Convert to DTO
```

### Layer 3 — Repository (DAO)

**Package:** `com.cognizant.healthcaregov.dao`

Repositories extend `JpaRepository<Entity, ID>`. They:
- Are interfaces — Spring Data JPA generates implementation at runtime
- Provide built-in CRUD methods (`save`, `findById`, `findAll`, `deleteById`, `count`, `existsById`)
- Support derived query methods (e.g., `findByEmail(String email)` — JPA parses the method name and generates SQL automatically)
- Support custom JPQL queries with `@Query` for complex filters

**Examples:**
```java
// Derived query — JPA generates: SELECT * FROM users WHERE email = ?
Optional<User> findByEmail(String email);

// Derived query — JPA generates: SELECT * FROM users WHERE status = ?
List<User> findByStatus(String status);

// Custom JPQL — explicit query needed for complex multi-param filtering
@Query("SELECT c FROM ComplianceRecord c WHERE " +
       "(:type IS NULL OR c.type = :type) AND " +
       "(:result IS NULL OR c.result = :result)")
List<ComplianceRecord> search(@Param("type") String type, @Param("result") String result, ...);
```

### Layer 4 — Entity

**Package:** `com.cognizant.healthcaregov.entity`

Entities are annotated with `@Entity` and represent database tables. They:
- Map directly to a database table (`@Table(name = "users")`)
- Use `@Id` and `@GeneratedValue` for primary keys
- Use `@Column` to configure column constraints
- Use `@ManyToOne`, `@OneToOne`, `@OneToMany` to define relationships
- Are never returned directly from the API — they are always converted to DTOs

## 2.2 DTOs (Data Transfer Objects)

**Package:** `com.cognizant.healthcaregov.dto`

DTOs are plain Java classes (POJOs) with Lombok annotations. There are two kinds:

| Type | Purpose | Example |
|---|---|---|
| Request DTO | Carries data from the client to the server | `PatientRegisterRequest`, `LoginRequest` |
| Response DTO | Carries data from the server to the client | `PatientResponse`, `AppointmentResponse` |

**Why DTOs and not Entities directly?**
- Entities can have fields you don't want to expose (e.g., `password` in `User`)
- DTOs let you shape the response exactly as needed
- Decouples your API contract from your database schema
- Prevents Jackson from serializing lazy-loaded JPA relationships (which causes `LazyInitializationException`)

**Example — User entity has a `password` field. UserResponse DTO does not:**
```java
// Entity (has password)
public class User {
    private String password; // NEVER expose this
}

// Response DTO (safe to send to client)
public class UserResponse {
    private Integer userID;
    private String name;
    private String role;
    private String email;
    private String phone;
    private String status;
    // No password field!
}
```

## 2.3 Request → Response Lifecycle

Here is the complete journey of a single HTTP request through the system:

```
Client → HTTP POST /api/patients/register
             │
             ▼
     JwtAuthenticationFilter
     (checks Authorization header,
      validates token, sets SecurityContext)
             │
             ▼ (no token needed for /register — it's public)
     PatientController.register()
     - @Valid triggers Bean Validation on PatientRegisterRequest
     - If validation fails → MethodArgumentNotValidException
     - If valid → calls patientService.register(req)
             │
             ▼
     PatientService.register()
     - Checks if email already exists → throws BadRequestException if so
     - Creates and saves User entity (status = "Pending")
     - Creates and saves Patient entity linked to that User
     - Calls toResponse(saved) to convert Patient → PatientResponse DTO
             │
             ▼
     PatientController receives PatientResponse
     - Wraps in ResponseEntity with 201 CREATED
             │
             ▼
     JSON serialization (Jackson converts PatientResponse to JSON)
             │
             ▼
Client ← HTTP 201 { "patientID": 1, "name": "John", ... }
```

## 2.4 Cross-Cutting Concerns

Two important mechanisms operate across all layers:

### AOP — Audit Logging (AuditAspect.java)

This is an **Aspect-Oriented Programming** component that automatically logs every service method call to the `audit_log` table — without any service class needing to call it explicitly.

```java
@Around("hospitalServiceMethods() || appointmentServiceMethods() || ...")
public Object logAudit(ProceedingJoinPoint jp) throws Throwable {
    String methodName = jp.getSignature().getName();
    String resource = jp.getTarget().getClass().getSimpleName()...;
    String username = SecurityContextHolder.getContext().getAuthentication().getName();
    String action = deriveAction(methodName); // e.g., "create" → "CREATE"
    
    Object result = jp.proceed(); // Execute the actual service method
    
    logService.savelog(username, action, resource); // Save audit log
    return result;
}
```

The `@Pointcut` annotations define which methods to intercept. The `@Around` advice wraps those methods and logs them. This is why every action (Create Hospital, Book Appointment, Record Treatment, etc.) automatically appears in audit logs.

### Global Exception Handler (GlobalExceptionHandler.java)

Annotated with `@RestControllerAdvice`, this catches all exceptions thrown from any layer and converts them into consistent JSON error responses. Without it, Spring would return raw stack traces or HTML error pages.

---

# 3. MODULE-WISE BREAKDOWN

## 3.1 Auth Module

**Files:** `AuthController`, `AuthService`, `JwtUtil`, `JwtAuthenticationFilter`, `CustomUserDetailsService`, `SecurityConfig`

### What it does
Handles user login and issues JWT tokens. Protects all endpoints based on roles.

### How login works (step by step)
1. Client sends `POST /api/users/login` with `{ email, password }`
2. `AuthController.login()` calls `AuthService.login()`
3. `AuthService` calls `authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(email, password))`
4. Spring Security internally calls `CustomUserDetailsService.loadUserByUsername(email)`
5. `loadUserByUsername` fetches the `User` from DB and returns a `UserDetails` with `ROLE_<ROLE>` authority
6. Spring Security compares the provided password against the BCrypt-encoded stored password
7. If credentials match, authentication succeeds
8. `JwtUtil.generateToken(userDetails)` creates a JWT token with the user's email as subject and their role as a claim
9. Token is returned as a plain string to the client

### JWT token structure
A JWT has 3 parts: **Header.Payload.Signature**, Base64 encoded and dot-separated.

The payload in this system contains:
```json
{
  "sub": "karthik09@gmail.com",
  "role": ["ROLE_ADMIN"],
  "iat": 1700000000,
  "exp": 1700007200
}
```

Token expiry is set to `2000 * 60 * 60` milliseconds = **2000 hours** (very long — simplified for development).

### How subsequent requests are authenticated
Every request (except login and patient registration) must include:
```
Authorization: Bearer <token>
```

`JwtAuthenticationFilter` (extends `OncePerRequestFilter`) intercepts every request:
```java
String token = authHeader.substring(7); // Strip "Bearer "
String email = jwtUtil.extractEmail(token);
UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
if (jwtUtil.validateToken(token, userDetails)) {
    // Set authentication in SecurityContext
    SecurityContextHolder.getContext().setAuthentication(authToken);
}
```

Once the `SecurityContext` has authentication, Spring Security's method-level and URL-level access rules apply.

## 3.2 User Management Module

**Files:** `UserController`, `UserService`, `UserRepository`

### What it does
- Lists users (with optional status filter)
- Allows admins to approve, reject, or deactivate user accounts

### Key logic — updateStatus
```java
public UserResponse updateStatus(Integer userId, UserStatusRequest req, Integer adminId) {
    // Validates that status is one of: Active, Inactive, Rejected
    if (!VALID_STATUSES.contains(req.getStatus())) {
        throw new BadRequestException("Invalid status...");
    }
    User user = userRepository.findById(userId)...;
    if (user.getRole().equals("PATIENT")) {
        user.setStatus(req.getStatus());
    }
    return toResponse(userRepository.save(user));
}
```

> Note: The current implementation only changes status for PATIENT role users. This is the admin approval mechanism — a newly registered patient starts with `status = "Pending"`. Admin sets it to `"Active"` to approve.

## 3.3 Patient Module

**Files:** `PatientController`, `PatientService`, `PatientRepository`, `PatientDocumentRepository`, `MedicalRecordRepository`

### What it does
- Patient self-registration (creates both a `User` and a `Patient` record)
- Profile viewing and updating
- Document upload (IDProof or HealthCard)
- View medical history (past treatments)
- View medical summary (MedicalRecord)

### Registration flow
When a patient registers:
1. A `User` record is created with `role = "Patient"` and `status = "Pending"`
2. A `Patient` record is created linked to that `User`, also with `status = "Pending"`
3. The patient **cannot log in** until Admin changes status to `"Active"`

### Ownership verification
Before any patient data is returned or modified, the service checks:
```java
private void verifyPatientOwnership(Patient patient, String action) {
    if (securityUtils.isAdmin() || securityUtils.hasRole("DOCTOR")) {
        return; // Admins and Doctors can always access
    }
    String callerEmail = securityUtils.getCurrentUserEmail().orElse(null);
    User linkedUser = patient.getUser();
    if (!linkedUser.getEmail().equalsIgnoreCase(callerEmail)) {
        throw new BadRequestException("Access denied: you may only " + action + " for your own account.");
    }
}
```
This ensures a patient can only view/update their own profile.

### Document upload
Patients upload identity and health documents. Only `"IDProof"` and `"HealthCard"` are accepted as doc types. The file itself is stored externally (represented by a `fileURI`). The database only stores the URI and metadata.

## 3.4 Hospital Management Module

**Files:** `HospitalController`, `HospitalService`, `HospitalRepository`

### What it does
Full CRUD (Create, Read, Update, Delete) for hospitals. Search by name or location.

### Search implementation
Uses a custom JPQL query:
```java
@Query("SELECT h FROM Hospital h WHERE " +
       "LOWER(h.name) LIKE LOWER(CONCAT('%',:q,'%')) OR " +
       "LOWER(h.location) LIKE LOWER(CONCAT('%',:q,'%'))")
List<Hospital> search(@Param("q") String query);
```
This is a **case-insensitive partial match** on both name and location.

### Access rules
- `POST`, `PUT`, `DELETE` → ADMIN only
- `GET` → Any authenticated user

## 3.5 Resource Management Module

**Files:** `ResourceController`, `ResourceService`, `ResourceRepository`

### What it does
Manages physical resources (Beds, Equipment, Staff) allocated to hospitals.

### Resource types
Exactly three types are used throughout the system: `"Beds"`, `"Equipment"`, `"Staff"`. These are used in analytics to compute totals.

### sumByType — used by Analytics
```java
@Query("SELECT COALESCE(SUM(r.quantity), 0) FROM Resource r WHERE r.type = :type")
Long sumQuantityByType(@Param("type") String type);
```
`COALESCE` returns 0 if there are no records (prevents null).

## 3.6 Schedule Module

**Files:** `ScheduleController`, `ScheduleService`, `ScheduleRepository`

### What it does
Doctors create time slots for their availability. Patients can then book those slots.

### Duplicate slot prevention
The `Schedule` table has a **unique constraint**:
```java
@Table(uniqueConstraints = @UniqueConstraint(
    name = "uk_schedule_doctor_date_slot",
    columnNames = {"doctorID", "available_date", "time_slot"}))
```
The service also checks this programmatically before saving:
```java
if (scheduleRepository.findByDoctorUserIDAndAvailableDateAndTimeSlot(...).isPresent()) {
    throw new BadRequestException("A slot already exists...");
}
```
There is **both DB-level and application-level protection** against duplicates.

### Slot status
A slot starts as `"Available"`. When a patient books it, it becomes `"Booked"`. When the appointment is cancelled, the service resets it to `"Available"`.

## 3.7 Appointment Module

**Files:** `AppointmentController`, `AppointmentService`, `AppointmentRepository`

### What it does
Patients book appointments against available schedule slots. Admins manage workflows.

### Booking flow
1. Patient sends `POST /api/appointments/book` with `patientID`, `doctorID`, `hospitalID`, `date`, `time`
2. Service finds the matching `Schedule` slot for that doctor/date/time
3. If no slot exists or slot is not `"Available"` → throws `BadRequestException`
4. Slot status is changed to `"Booked"`
5. `Appointment` is created with `status = "Confirmed"`
6. A `Notification` is sent to the patient

### Cancellation flow
1. Patient sends `PUT /api/appointments/cancel` with `appointmentID`
2. Service verifies that the calling user is the patient who owns the appointment
3. The matched schedule slot is reverted to `"Available"`
4. Appointment status is set to `"Cancelled"`
5. A `Notification` is sent to the patient

### Admin operations
- **Reassign:** Admin can change the doctor on an existing appointment
- **Check-in:** Admin marks a patient as `"Arrived"` when they physically arrive

## 3.8 Treatment Module

**Files:** `TreatmentController`, `TreatmentService`, `TreatmentRepository`, `MedicalRecordRepository`

### What it does
Doctors record diagnoses, prescriptions, and notes. This updates the patient's MedicalRecord automatically.

### Treatment recording flow
1. Doctor calls `POST /api/treatments`
2. `TreatmentService.record()` saves a `Treatment` entity
3. It then finds or creates a `MedicalRecord` for the patient and appends the diagnosis to `detailsJSON`
4. A `Notification` is sent to the doctor confirming the treatment was recorded

### MedicalRecord update
Doctors and Admins can also update the `Patient` entity's core fields (name, contactInfo, status) via `PUT /api/treatments/patients/{patientId}`. Records marked `"Finalized"` cannot be updated.

## 3.9 Compliance Module

**Files:** `ComplianceController`, `ComplianceService`, `ComplianceRecordRepository`

### What it does
Compliance Officers record whether appointments, treatments, or hospitals pass or fail policy checks.

### Compliance record types
`type` must be one of: `"Appointment"`, `"Treatment"`, `"Hospital"`
`result` must be: `"Pass"` or `"Fail"`

### Deadline alerts (notification integration)
When a compliance record is created, the system automatically sends a notification to the officer:
```java
notificationService.send(officerId, saved.getComplianceID(),
    "Compliance record created for " + saved.getType() + " ... Review for upcoming deadlines.",
    "Compliance");
```

### Audit log access
`GET /api/compliance/audit-logs` returns all audit log entries. This is how compliance officers review what actions have been taken across the system.

## 3.10 Audit Module

**Files:** `AuditController`, `AuditService`, `AuditRepository`

### What it does
Government auditors create formal audit records with scope and findings.

### Status transitions
Audits follow a strict state machine:
```
Scheduled → In-Progress → Completed
```
The service enforces this:
```java
boolean valid = switch (from) {
    case "Scheduled"   -> List.of("Scheduled", "In-Progress").contains(to);
    case "In-Progress" -> List.of("In-Progress", "Completed").contains(to);
    case "Completed"   -> to.equals("Completed");  // Read-only
    default -> false;
};
if (!valid) throw new BadRequestException("Invalid status transition...");
```

Once an audit is `"Completed"`, it can only be modified by users with the `Auditor` or `Government_Auditor` role.

## 3.11 Analytics Module

**Files:** `AnalyticsController`, `AnalyticsService`

### What it does
Provides high-level metrics about hospital resources and capacity. Accessible to ADMINs and PROGRAM_MANAGERs.

### Endpoints
- `GET /api/analytics/hospitals` → total hospitals, total beds, equipment, staff, capacity
- `GET /api/analytics/reports/hospital-capacity` → total capacity
- `GET /api/analytics/reports/resource-availability` → beds/equipment/staff counts
- `GET /api/analytics/reports/resource-distribution` → same as availability (re-uses the same data)

## 3.12 Program Module

**Files:** `ProgramController`, `ProgramService`

### What it does
The Program Manager module gives an aggregated view of the entire program's performance plus the ability to generate scoped reports.

### Dashboard
`GET /api/program/dashboard` returns aggregate counts:
- Appointments (total, confirmed, cancelled)
- Treatments (total, active, completed)
- Hospitals (count, total capacity)
- Compliance records (total, passed, failed)

Supports optional `startDate` / `endDate` query params to filter appointment data.

### Report generation
`POST /api/program/reports` generates a `Report` entity in the DB with a JSON `metrics` string computed from live data. The scope can be: `Appointment`, `Treatment`, `Hospital`, or `Compliance`.

## 3.13 Notification Module

**Files:** `NotificationController`, `NotificationService`, `NotificationRepository`

### What it does
Notifications are created **automatically** by other services (never directly by the user). Users retrieve their own notifications via the API.

### Events that trigger notifications
| Event | Who gets notified | Category |
|---|---|---|
| Appointment booked | Patient | Appointment |
| Appointment cancelled | Patient | Appointment |
| Treatment recorded | Doctor | Treatment |
| Compliance record created | Compliance Officer | Compliance |

### Access control
A user can only read their own notifications. Admins can read any user's notifications.

---

# 4. DATABASE DESIGN

## 4.1 Entity Relationship Overview

```
User (1) ──────────── (1) Patient
 │
 │ (1-to-many)
 ├──── Schedule (doctor's time slots)
 ├──── Appointment (as doctor)
 ├──── Treatment (as doctor)
 ├──── AuditLog
 ├──── Notification
 └──── Audit (as officer)

Patient (1) ──────────── (many) PatientDocument
Patient (1) ──────────── (many) Treatment
Patient (1) ──────────── (1)    MedicalRecord
Patient (1) ──────────── (many) Appointment

Hospital (1) ──────────── (many) Resource
Hospital (1) ──────────── (many) Schedule
Hospital (1) ──────────── (many) Appointment
Hospital (1) ──────────── (many) Report
```

## 4.2 Entities and Key Fields

### User
| Field | Type | Purpose |
|---|---|---|
| userID | INT (PK, auto) | Primary key |
| name | VARCHAR (not null) | Display name |
| role | VARCHAR (not null) | One of: Admin, Doctor, Patient, Program_Manager, Comp_Officer, Auditor |
| email | VARCHAR (unique, not null) | Login credential / username |
| phone | VARCHAR | Contact number |
| status | VARCHAR (not null) | Pending / Active / Inactive / Rejected / APPROVED |
| password | VARCHAR (not null) | BCrypt-encoded password |

### Patient
| Field | Type | Purpose |
|---|---|---|
| patientID | INT (PK, auto) | Primary key |
| userID | INT (FK → User) | Links to login account (nullable) |
| name | VARCHAR | Patient name |
| dob | DATE | Date of birth |
| gender | VARCHAR | Male/Female/Other |
| address | TEXT | Physical address |
| contactInfo | VARCHAR | 10-digit phone number |
| status | VARCHAR | Pending / Active / Inactive / Finalized |

**Key relationship:** `Patient` has a one-to-one FK to `User`. When a patient registers via the API, both a `User` record and a `Patient` record are created simultaneously and linked.

### Appointment
| Field | Type | Purpose |
|---|---|---|
| appointmentID | INT (PK, auto) | Primary key |
| patientID | INT (FK → Patient) | Who is the appointment for |
| doctorID | INT (FK → User) | Attending doctor |
| hospitalID | INT (FK → Hospital) | Where |
| date | DATE | Appointment date |
| time | TIME | Appointment time |
| status | VARCHAR | Confirmed / Cancelled / Arrived |

### Schedule
| Field | Type | Purpose |
|---|---|---|
| scheduleID | INT (PK, auto) | Primary key |
| doctorID | INT (FK → User) | Doctor offering this slot |
| hospitalID | INT (FK → Hospital) | Hospital where slot is offered |
| availableDate | DATE | Date of availability |
| timeSlot | VARCHAR | Time (e.g., "10:00:00") |
| status | VARCHAR | Available / Booked |

**Unique constraint:** `(doctorID, available_date, time_slot)` — no doctor can have two slots at the same time.

### Treatment
| Field | Type | Purpose |
|---|---|---|
| treatmentID | INT (PK, auto) | Primary key |
| patientID | INT (FK → Patient) | Patient being treated |
| doctorID | INT (FK → User) | Treating doctor |
| diagnosis | VARCHAR | Diagnosis text |
| prescription | VARCHAR | Prescribed medication/plan |
| treatmentNotes | TEXT | Additional notes |
| date | DATE (auto) | Auto-set on creation |
| status | VARCHAR | Active / Completed |

### MedicalRecord
| Field | Type | Purpose |
|---|---|---|
| recordID | INT (PK, auto) | Primary key |
| patientID | INT (FK → Patient) | One record per patient |
| detailsJSON | TEXT | Accumulated diagnosis history as text |
| date | DATE (auto) | Last updated date |
| status | VARCHAR | Active / Finalized |

> Note: `detailsJSON` is not actually JSON-formatted in the current implementation. It's a plain text string that concatenates diagnosis entries: `" | Diagnosis: Fever | Diagnosis: Hypertension"`

### Hospital
| Field | Type | Purpose |
|---|---|---|
| hospitalID | INT (PK, auto) | Primary key |
| name | VARCHAR | Hospital name |
| location | VARCHAR | City/address |
| capacity | INT | Total patient capacity |
| status | VARCHAR | Active / Inactive |

### Resource
| Field | Type | Purpose |
|---|---|---|
| resourceID | INT (PK, auto) | Primary key |
| hospitalID | INT (FK → Hospital) | Belongs to which hospital |
| type | VARCHAR | Beds / Equipment / Staff |
| quantity | INT | Count of this resource |
| status | VARCHAR | Available / In-Use |

### ComplianceRecord
| Field | Type | Purpose |
|---|---|---|
| complianceID | INT (PK, auto) | Primary key |
| entityId | INT | ID of the entity being checked |
| type | VARCHAR | Appointment / Treatment / Hospital |
| result | VARCHAR | Pass / Fail |
| date | DATE (auto) | Auto-set on creation |
| notes | TEXT | Officer's notes |

### Audit
| Field | Type | Purpose |
|---|---|---|
| auditID | INT (PK, auto) | Primary key |
| officerID | INT (FK → User) | Auditor conducting the audit |
| scope | VARCHAR | What is being audited |
| findings | TEXT | Auditor's findings |
| date | DATE (auto) | Auto-set on creation |
| status | VARCHAR | Scheduled / In-Progress / Completed |

### AuditLog
| Field | Type | Purpose |
|---|---|---|
| auditLogID | INT (PK, auto) | Primary key |
| userID | INT (FK → User) | Who performed the action |
| action | VARCHAR | CREATE / UPDATE / DELETE / VIEW_ALL etc. |
| resource | VARCHAR | Which service (Hospital, Appointment, etc.) |
| timestamp | INSTANT (auto) | Exact time of action |

### Notification
| Field | Type | Purpose |
|---|---|---|
| notificationID | INT (PK, auto) | Primary key |
| userID | INT (FK → User) | Who receives the notification |
| entityId | INT | ID of the related entity |
| message | TEXT | Notification text |
| category | VARCHAR | Appointment / Treatment / Compliance |
| status | VARCHAR | Unread / Read |
| createdDate | DATETIME (auto) | Auto-set on creation |

### Report
| Field | Type | Purpose |
|---|---|---|
| reportID | INT (PK, auto) | Primary key |
| hospitalID | INT (FK → Hospital) | For which hospital |
| scope | VARCHAR | Appointment / Treatment / Hospital / Compliance |
| metrics | TEXT | JSON string of computed metrics |
| generatedDate | DATETIME (auto) | Auto-set on creation |

## 4.3 Status Handling

### Patient status lifecycle
```
[Registration] → status = "Pending"
      │
      ▼ (Admin approves via PUT /api/users/{id}/status)
   "Active"  ←→  "Inactive"  (Admin can toggle)
      │
      ▼ (Admin sets)
   "Rejected"  (cannot log in)
      │
      ▼ (set by doctor/admin via TreatmentService)
   "Finalized" (medical record locked)
```

### Appointment status lifecycle
```
[Booking] → "Confirmed"
     │
     ├──→ "Cancelled"  (patient cancels)
     │
     └──→ "Arrived"    (admin checks in patient)
```

### Schedule slot status
```
[Doctor creates] → "Available"
        │
        ▼ (patient books)
     "Booked"
        │
        ▼ (appointment cancelled)
     "Available"  (reverted)
```

### Audit status lifecycle
```
[Create] → "Scheduled"
     │
     ▼ (auditor updates)
 "In-Progress"
     │
     ▼ (auditor finalizes)
 "Completed"  → read-only for non-auditors
```

---

# 5. AUTHENTICATION & AUTHORIZATION

## 5.1 Spring Security Overview

Spring Security is configured in `SecurityConfig.java`. It sets up:
1. **Stateless sessions** — no server-side sessions, every request must carry a JWT
2. **CSRF disabled** — appropriate for stateless REST APIs
3. **URL-based access rules** — specific roles required for specific endpoints
4. **Custom JWT filter** — runs before Spring's built-in authentication

## 5.2 Password Encoding

Passwords are never stored in plain text. `BCryptPasswordEncoder` is used:
```java
@Bean
public PasswordEncoder passwordEncoder() {
    return new BCryptPasswordEncoder();
}
```
When a user is saved:
```java
user.setPassword(passwordEncoder.encode(req.getPassword()));
```
When a user logs in, Spring Security calls `passwordEncoder.matches(rawPassword, encodedPassword)` internally.

## 5.3 Custom UserDetailsService

`CustomUserDetailsService` tells Spring Security how to load a user from the database:
```java
public UserDetails loadUserByUsername(String email) {
    User user = userRepository.findByEmail(email).orElseThrow(...);
    SimpleGrantedAuthority authority = new SimpleGrantedAuthority("ROLE_" + user.getRole().toUpperCase());
    return new org.springframework.security.core.userdetails.User(
        user.getEmail(), user.getPassword(), List.of(authority));
}
```

This is critical — it maps the `role` field from the database to a `ROLE_XXX` authority that Spring Security understands. For example, a user with `role = "ADMIN"` gets authority `"ROLE_ADMIN"`.

## 5.4 JWT Utility (JwtUtil.java)

Key methods:
- `generateToken(userDetails)` — creates a signed JWT with email as subject and role as a claim
- `extractEmail(token)` — reads the subject (email) from the token
- `validateToken(token, userDetails)` — checks that email matches and token hasn't expired
- `getSignKey()` — derives an HMAC-SHA256 key from the secret configured in `application.properties`

The secret key is: `HealthCareGov2025SuperSecretKeyForJWTSigningMustBe256BitsLong!!`

## 5.5 JWT Authentication Filter

`JwtAuthenticationFilter` runs on **every request** before any controller is called:

```java
String authHeader = request.getHeader("Authorization");
if (authHeader == null || !authHeader.startsWith("Bearer ")) {
    filterChain.doFilter(request, response); // Pass through (public endpoints)
    return;
}
String token = authHeader.substring(7);
String email = jwtUtil.extractEmail(token);
if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
    UserDetails userDetails = customUserDetailsService.loadUserByUsername(email);
    if (jwtUtil.validateToken(token, userDetails)) {
        UsernamePasswordAuthenticationToken authToken = 
            new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
        SecurityContextHolder.getContext().setAuthentication(authToken); // Mark as authenticated
    }
}
filterChain.doFilter(request, response);
```

After this filter runs, the `SecurityContext` holds the authenticated user. Subsequent authorization checks (role-based) use this context.

## 5.6 URL-Based Access Control

In `SecurityConfig.securityFilterChain()`:

```java
.authorizeHttpRequests(auth -> auth
    // Public endpoints
    .requestMatchers(HttpMethod.POST, "/api/users/login").permitAll()
    .requestMatchers(HttpMethod.POST, "/api/patients/register").permitAll()
    
    // Admin only
    .requestMatchers(HttpMethod.GET,  "/api/users").hasRole("ADMIN")
    .requestMatchers(HttpMethod.POST, "/api/hospitals").hasRole("ADMIN")
    
    // Multiple roles
    .requestMatchers(HttpMethod.GET, "/api/patients/*/history").hasAnyRole("PATIENT","DOCTOR","ADMIN")
    
    // Doctor only
    .requestMatchers(HttpMethod.POST, "/api/schedules").hasRole("DOCTOR")
    .requestMatchers(HttpMethod.POST, "/api/treatments").hasRole("DOCTOR")
    
    // Patient only
    .requestMatchers(HttpMethod.POST, "/api/appointments/book").hasRole("PATIENT")
    .requestMatchers(HttpMethod.PUT,  "/api/appointments/cancel").hasRole("PATIENT")
    
    .anyRequest().denyAll() // Everything else is blocked
)
```

`hasRole("ADMIN")` checks if the SecurityContext has authority `"ROLE_ADMIN"`.

## 5.7 Error Handlers for Security

Two components handle security failures:

**JwtAuthenticationEntryPoint** — triggers when a request has no valid JWT:
```json
{ "status": 401, "message": "Unauthorized: valid JWT token is required...", "timestamp": ... }
```

**JwtAccessDeniedHandler** — triggers when a valid JWT exists but the role doesn't have permission:
```json
{ "status": 403, "message": "Access denied: your role does not have permission...", "timestamp": ... }
```

## 5.8 SecurityUtils Helper

`SecurityUtils` (injected into Service classes) provides convenience methods:
```java
public Optional<String> getCurrentUserEmail()  // Gets email from SecurityContext
public boolean hasRole(String role)             // Checks if current user has a role
public boolean isAdmin()                        // Shorthand for hasRole("ADMIN")
```

This is how services enforce record-level ownership (e.g., a patient can only see their own profile).

---

# 6. API FLOW EXPLANATION

## 6.1 Login Flow (Step-by-Step)

```
Step 1: Client sends:
POST /api/users/login
{
  "email": "karthik09@gmail.com",
  "password": "karthik098"
}

Step 2: JwtAuthenticationFilter runs.
No "Authorization" header → passes through to controller.

Step 3: AuthController.login() is called.
@Valid annotation triggers Bean Validation:
- email must be a valid email format
- password must be >= 8 characters

Step 4: AuthService.login() is called.
authenticationManager.authenticate(
  new UsernamePasswordAuthenticationToken("karthik09@gmail.com", "karthik098")
)

Step 5: Spring Security calls CustomUserDetailsService.loadUserByUsername("karthik09@gmail.com")
→ Fetches User from DB
→ Returns UserDetails with authority "ROLE_ADMIN"

Step 6: Spring Security compares "karthik098" against the BCrypt-encoded stored password.
→ Match → Authentication succeeds

Step 7: JwtUtil.generateToken(userDetails) creates JWT:
{
  "sub": "karthik09@gmail.com",
  "role": ["ROLE_ADMIN"],
  "iat": 1700000000,
  "exp": 1707200000
}
→ Signed with HMAC-SHA256 using the secret key

Step 8: Token string returned to client:
"eyJhbGciOiJIUzI1NiJ9.eyJyb2xlIjpbIlJPTEVfQURNSU4iXSwic3ViIjoiZ..."

Step 9: Client stores this token and sends it as:
Authorization: Bearer eyJhbGciOiJIUzI1NiJ9...
in all future requests.
```

## 6.2 Patient Registration Flow (Pending State)

```
Step 1: Client sends (no auth token needed):
POST /api/patients/register
{
  "name": "John Doe",
  "dob": "1995-06-15",
  "gender": "Male",
  "address": "123 Main St, Chennai",
  "contactInfo": "9876543210",
  "email": "johndoe@example.com",
  "password": "password123"
}

Step 2: Bean Validation runs on PatientRegisterRequest:
- name: @NotBlank ✓
- contactInfo: @Pattern(regexp="\\d{10}") ✓ (10 digits)
- email: @Email ✓
- password: @Size(min=8) ✓

Step 3: PatientService.register() is called.

Step 4: Check for duplicate email:
userRepository.findByEmail("johndoe@example.com")
→ Not found → proceed

Step 5: Create User entity:
user.setRole("Patient")
user.setStatus("Pending")         ← STATUS IS PENDING
user.setPassword(BCrypt("password123"))
savedUser = userRepository.save(user)  → INSERT into users table

Step 6: Create Patient entity linked to the User:
patient.setUser(savedUser)
patient.setStatus("Pending")         ← PATIENT ALSO PENDING
saved = patientRepository.save(patient)  → INSERT into patient table

Step 7: Convert to DTO and return:
HTTP 201 Created
{
  "patientID": 1,
  "userID": 11,
  "name": "John Doe",
  "dob": "1995-06-15",
  "gender": "Male",
  "address": "123 Main St, Chennai",
  "contactInfo": "9876543210",
  "status": "Pending"
}

At this point, if John Doe tries to log in, Spring Security will authenticate him
successfully (credentials are correct). However, the application has no additional
login block based on "Pending" status in the current implementation — the status
is more of a profile/access indicator for admin management.
```

## 6.3 Admin Approval Flow

```
Step 1: Admin logs in and gets a token with role ROLE_ADMIN.

Step 2: Admin queries all pending users:
GET /api/users?status=Pending
Authorization: Bearer <admin-token>

Step 3: Spring Security checks:
- JwtAuthenticationFilter validates the token → sets SecurityContext with ROLE_ADMIN
- SecurityConfig: GET /api/users requires hasRole("ADMIN") ✓

Step 4: UserService.getUsers("Pending") runs:
userRepository.findByStatus("Pending")
→ Returns list of users with status = "Pending"
→ Converted to List<UserResponse> and returned

Step 5: Admin approves a specific user (e.g., userID = 11):
PUT /api/users/11/status?adminId=3
Authorization: Bearer <admin-token>
{
  "status": "Active"
}

Step 6: UserService.updateStatus(11, req, 3) runs:
- Validates "Active" is in VALID_STATUSES {Active, Inactive, Rejected}
- Fetches User with ID 11
- If user.getRole().equals("PATIENT") → sets status = "Active"
- Saves and returns updated UserResponse

Step 7: Response:
HTTP 200 OK
{
  "userID": 11,
  "name": "John Doe",
  "role": "Patient",
  "email": "johndoe@example.com",
  "status": "Active"
}
```

## 6.4 End-to-End Appointment Booking Flow

```
Step 1: Doctor creates a schedule slot:
POST /api/schedules  (DOCTOR role required)
{ "doctorId": 1, "hospitalId": 1, "availableDate": "2025-08-01", "timeSlot": "10:00:00" }
→ Schedule created with status = "Available"

Step 2: Patient (John Doe, approved) logs in and gets token.

Step 3: Patient books appointment:
POST /api/appointments/book  (PATIENT role required)
{ "patientID": 1, "doctorID": 1, "hospitalID": 1, "date": "2025-08-01", "time": "10:00:00" }

Step 4: AppointmentService.book() runs:
a) Formats time as "10:00:00"
b) Calls scheduleService.findSlot(1, 2025-08-01, "10:00:00")
   → Finds schedule with status = "Available" ✓
c) Fetches Patient 1, Doctor 1, Hospital 1 from DB
d) Sets slot status = "Booked", saves slot
e) Creates Appointment: status = "Confirmed"
f) Sends notification to patient's userID

Step 5: Response:
HTTP 201 Created
{
  "appointmentID": 1,
  "patientID": 1, "patientName": "John Doe",
  "doctorID": 1, "doctorName": "Shiva",
  "hospitalID": 1, "hospitalName": "Apollo Hospital",
  "date": "2025-08-01", "time": "10:00:00",
  "status": "Confirmed"
}

Step 6: Patient receives a notification:
GET /api/notifications/11  (userID 11)
→ [{ "message": "Your appointment with Dr. Shiva on 2025-08-01 at 10:00 is confirmed.", 
     "category": "Appointment", "status": "Unread" }]
```

---

# 7. VALIDATION & ERROR HANDLING

## 7.1 Bean Validation

Request DTOs use `javax.validation` (Jakarta Validation) annotations. The `@Valid` annotation on the controller method parameter triggers validation before the service is called.

**Validation annotations used in this project:**

| Annotation | What it checks | Example field |
|---|---|---|
| `@NotNull` | Field is not null | `patientId` in TreatmentRequest |
| `@NotBlank` | String is not null, empty, or whitespace | `name` in PatientRegisterRequest |
| `@Email` | Valid email format | `email` in LoginRequest |
| `@Pattern(regexp=...)` | Matches regex | `contactInfo`: `"\\d{10}"` (10 digits) |
| `@Size(min=...)` | String length constraint | `password`: `min=8` |
| `@Min(value=...)` | Minimum numeric value | `capacity`: `min=1` |
| `@Max(value=...)` | Maximum numeric value | `capacity`: `max=10000` |

**Example:**
```java
public class PatientRegisterRequest {
    @NotBlank(message = "Name is required")
    private String name;

    @Pattern(regexp = "\\d{10}", message = "Contact number must be exactly 10 digits")
    private String contactInfo;

    @Email(message = "Valid email is required")
    @NotBlank(message = "Email is required")
    private String email;

    @Size(min = 8, message = "Password should be at least 8 letters long")
    private String password;
}
```

If any field fails validation, Spring throws `MethodArgumentNotValidException` before the controller method body even runs.

## 7.2 Custom Exceptions

The project defines a hierarchy of custom exceptions:

```
AppException (extends RuntimeException)
├── BadRequestException     → HTTP 400
└── ResourceNotFoundException → HTTP 404
```

```java
// Base class
public class AppException extends RuntimeException {
    private final HttpStatus status;
    public AppException(String message, HttpStatus status) {
        super(message);
        this.status = status;
    }
}

// Usage in service
throw new BadRequestException("Email is already registered: " + req.getEmail());
throw new ResourceNotFoundException("Patient not found with id: " + id);
```

## 7.3 Global Exception Handler

`GlobalExceptionHandler` (annotated `@RestControllerAdvice`) catches all exceptions and formats them consistently:

```java
// Catches: BadRequestException, ResourceNotFoundException
@ExceptionHandler(AppException.class)
public ResponseEntity<ErrorResponse> handleAppException(AppException ex) {
    return build(ex.getStatus().value(), ex.getMessage(), ex.getStatus());
}

// Catches: duplicate email, duplicate schedule slot, FK constraint violations
@ExceptionHandler(DataIntegrityViolationException.class)
public ResponseEntity<ErrorResponse> handleDataIntegrity(...) {
    String msg = "Database constraint violation...";
    if (detail.contains("email")) msg = "Email address is already registered.";
    if (detail.contains("uk_schedule")) msg = "A schedule slot already exists...";
    // Returns HTTP 409 Conflict
}

// Catches: Bean Validation failures
@ExceptionHandler(MethodArgumentNotValidException.class)
public ResponseEntity<ErrorResponse> handleValidation(...) {
    String msg = ex.getBindingResult().getFieldErrors().stream()
        .map(e -> e.getField() + ": " + e.getDefaultMessage())
        .findFirst().orElse("Validation error");
    // Returns HTTP 400 Bad Request
}

// Catches: type mismatches in path variables/query params
@ExceptionHandler(MethodArgumentTypeMismatchException.class)
// Returns HTTP 400 with "Parameter 'X' must be of type Y"

// Catches: anything else
@ExceptionHandler(Exception.class)
// Returns HTTP 500 Internal Server Error
```

## 7.4 Error Response Format

All errors are returned in this consistent structure:
```json
{
  "status": 400,
  "message": "contactInfo: Contact number must be exactly 10 digits",
  "timestamp": 1700000000000
}
```

This is defined by the `ErrorResponse` class:
```java
@Getter @AllArgsConstructor
public class ErrorResponse {
    private final int status;
    private final String message;
    private final long timestamp;
}
```

## 7.5 Business Rule Validation in Service Layer

Beyond Bean Validation, services enforce business rules manually:

```java
// Check valid status values
if (!VALID_STATUSES.contains(req.getStatus())) {
    throw new BadRequestException("Invalid status. Allowed: Active, Inactive, Rejected");
}

// Check for cancelled appointment before cancel again
if ("Cancelled".equalsIgnoreCase(appt.getStatus())) {
    throw new BadRequestException("Appointment is already cancelled.");
}

// Check for Finalized patient before update
if ("Finalized".equalsIgnoreCase(patient.getStatus())) {
    throw new BadRequestException("Cannot update a Finalized patient record.");
}

// Check audit status transition
boolean valid = switch (from) {
    case "Scheduled" -> List.of("Scheduled", "In-Progress").contains(to);
    // ...
};
if (!valid) throw new BadRequestException("Invalid status transition...");
```

---

# 8. KEY DESIGN DECISIONS

## 8.1 Why DTOs Instead of Entities?

**Problem with returning entities directly:**
- `User` entity has a `password` field — exposing it is a security violation
- JPA lazy-loaded relationships (e.g., `Patient.user`) would cause `LazyInitializationException` outside of a transaction when Jackson tries to serialize them
- Entities are tightly coupled to the DB schema — changing DB structure would break the API contract

**Solution — DTOs:**
- Request DTOs define exactly what the client must send (with validation annotations)
- Response DTOs define exactly what the client receives (only safe, relevant fields)
- The `toResponse()` private method in each service handles the conversion

## 8.2 Why Layered Architecture?

**Single Responsibility Principle:** Each layer has exactly one job:
- Controller: HTTP handling
- Service: Business logic
- Repository: Data access

**Testability:** Each layer can be tested independently:
- Controller tests mock the Service
- Service tests mock the Repository
- Repository tests use an in-memory database

**Maintainability:** Business logic is centralized in Services — not scattered across Controllers. If a business rule changes, you change one service method.

## 8.3 Why Spring Security + JWT?

**Stateless:** JWT means the server doesn't need to store session state. Any server instance can validate any token just by having the secret key. This is essential for horizontal scaling.

**Role-Based:** The `ROLE_XXX` authority model maps cleanly to `hasRole()` checks in security configuration, making access control declarative and centralized.

**Separation of Auth Logic:** The `JwtAuthenticationFilter` handles token validation. The `SecurityConfig` handles authorization rules. The `AuthService` handles login. These are separate concerns, each in the right place.

## 8.4 Why AOP for Audit Logging?

Without AOP, every service method would need to contain code like:
```java
// At the start of every method in every service
auditLogService.save(username, "CREATE", "Hospital");
```

This violates the **DRY principle** (Don't Repeat Yourself) and mixes audit concerns with business logic.

With `@Aspect + @Around`, the audit logging is completely transparent to the service classes — they don't know they're being logged. If the audit logging requirement changes, you change one class (`AuditAspect`).

## 8.5 Why Bi-Level Duplicate Prevention for Schedules?

The schedule duplicate check is done both in the service layer AND at the database level (unique constraint).

**Why both?**
- The service-level check gives a friendly, specific error message before hitting the DB
- The DB constraint is a safety net in case two concurrent requests bypass the service check at the same millisecond (a race condition)

This is a standard pattern for preventing duplicates in concurrent systems.

## 8.6 @Transactional Usage

Services use `@Transactional` to wrap operations that touch multiple DB tables:

```java
@Transactional  // Applied to the whole method
public PatientResponse register(PatientRegisterRequest req) {
    // Saves User AND Patient in one transaction
    User savedUser = userRepository.save(user);
    Patient saved = patientRepository.save(patient);
    // If Patient save fails, User save is also rolled back
    return toResponse(saved);
}
```

Read-only operations use `@Transactional(readOnly = true)` — this is a performance hint to the database (no locking needed for reads).

The `LogService.savelog()` uses `@Transactional(propagation = Propagation.REQUIRES_NEW)` — this creates a **separate transaction** for audit logging. Even if the main service transaction fails (and rolls back), the audit log of the failure is still saved.

## 8.7 Pre-Seeded Users (No User Registration API)

User registration for non-patient roles (Doctor, Admin, Program Manager, etc.) is handled through a `DatabaseSeeder` (`CommandLineRunner`). This runs at application startup:

```java
@Override
public void run(String... args) throws Exception {
    if (userRepository.count() == 0) {
        // Create 10 users with different roles
    }
}
```

The check `userRepository.count() == 0` prevents re-seeding on every restart. This is an MVP trade-off — in production, an admin would create other users through a proper admin interface.

---

# 9. HOW TO RUN THE PROJECT

## 9.1 Prerequisites

| Tool | Version | Purpose |
|---|---|---|
| Java | 17 | Runtime and compilation |
| Maven | 3.9+ (or use ./mvnw) | Build tool |
| MySQL | 8.0+ | Database |

## 9.2 Database Setup

```sql
-- Create the database
CREATE DATABASE healthcaregov;
CREATE USER 'root'@'localhost' IDENTIFIED BY 'root';
GRANT ALL PRIVILEGES ON healthcaregov.* TO 'root'@'localhost';
FLUSH PRIVILEGES;
```

The application uses `spring.jpa.hibernate.ddl-auto=update` — this means Hibernate **automatically creates/updates the tables** on startup. You do NOT need to run any SQL scripts to create tables.

## 9.3 Configuration (application.properties)

Located at `src/main/resources/application.properties`:

```properties
spring.application.name=healthcaregov
server.port=9090

spring.datasource.url=jdbc:mysql://localhost:3306/healthcaregov
spring.datasource.username=root
spring.datasource.password=root
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true

logging.level.com.cognizant.healthcaregov=DEBUG
logging.level.org.springframework.security=WARN

secretKey=HealthCareGov2025SuperSecretKeyForJWTSigningMustBe256BitsLong!!
```

**Key configs:**
- `server.port=9090` — API runs on port 9090, not the default 8080
- `ddl-auto=update` — Auto-creates/updates tables
- `show-sql=true` — Prints all SQL to console (useful for debugging)
- `secretKey` — JWT signing key; must be the same across restarts

## 9.4 Build and Run

```bash
# Option 1: Using Maven Wrapper (no Maven installation needed)
./mvnw spring-boot:run

# Option 2: Build JAR and run
./mvnw clean package -DskipTests
java -jar target/healthcaregov-0.0.1-SNAPSHOT.jar

# Option 3: From IDE (IntelliJ/Eclipse)
# Run HealthcaregovApplication.java as a Java Application
```

## 9.5 What Happens on First Startup

1. Hibernate creates all tables in the `healthcaregov` database
2. `DatabaseSeeder.run()` executes:
   - Checks `userRepository.count() == 0`
   - Creates 10 seeded users (2 Admins, 2 Doctors, 2 Program Managers, 2 Compliance Officers, 2 Auditors)
3. Application is ready at `http://localhost:9090`

## 9.6 Testing the API

1. Use Postman or any HTTP client
2. First call: `POST http://localhost:9090/api/users/login`
3. Body: `{ "email": "karthik09@gmail.com", "password": "karthik098" }`
4. Copy the returned JWT string
5. Add header to all subsequent requests: `Authorization: Bearer <token>`

---

# QUICK REVISION SUMMARY

| Topic | Key Points |
|---|---|
| **Architecture** | Controller → Service → Repository → DB. DTOs separate API from entities. |
| **Auth** | JWT token issued on login, validated on every request via JwtAuthenticationFilter. |
| **Roles** | ADMIN, DOCTOR, PATIENT, PROGRAM_MANAGER, COMP_OFFICER, AUDITOR |
| **Patient flow** | Register → status=Pending → Admin sets Active → Patient can use system |
| **Appointment flow** | Doctor creates slot (Available) → Patient books → slot=Booked, appt=Confirmed → Cancel → slot=Available |
| **Audit logging** | Automatic via AOP (@Around advice) — logs every service method call |
| **Validation** | Bean Validation on Request DTOs (@NotBlank, @Email, @Pattern) + business rules in Services |
| **Error handling** | GlobalExceptionHandler converts all exceptions to { status, message, timestamp } JSON |
| **Transactions** | @Transactional wraps multi-table operations. REQUIRES_NEW for audit logs (survives rollbacks). |
| **Notifications** | Auto-created by services on key events — appointment booking/cancellation, treatment recording, compliance creation |
| **Status machines** | Patient: Pending→Active/Rejected/Inactive. Appointment: Confirmed→Cancelled/Arrived. Audit: Scheduled→In-Progress→Completed |
| **Port** | Runs on 9090 (not 8080) |
| **DB setup** | MySQL, auto DDL with `ddl-auto=update`, no manual SQL scripts needed |

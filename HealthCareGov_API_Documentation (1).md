# HealthCareGov — API Documentation

**Base URL:** `http://localhost:9090`  
**Content-Type:** `application/json`  
**Authentication:** JWT Bearer Token

---

## Authentication Flow

1. Call `POST /api/users/login` with email and password.
2. Copy the JWT token from the response.
3. Add the header `Authorization: Bearer <token>` to every subsequent request.

> Users are pre-seeded in the system. Patient self-registration creates a **Pending** account; an Admin must approve it before the patient can log in.

---

## Roles

| Role | Description |
|---|---|
| `ADMIN` | Full system access — approves users, manages hospitals, oversees workflows |
| `DOCTOR` | Manages schedules, records treatments |
| `PATIENT` | Books/cancels appointments, views own records |
| `PROGRAM_MANAGER` | Views dashboard and generates reports |
| `COMP_OFFICER` | Creates and manages compliance records |
| `AUDITOR` | Creates and reviews audits |

---

## 1. Auth

### Login
`POST /api/users/login`  
**Access:** Public  
**Request Body:**
```json
{
  "email": "karthik09@gmail.com",
  "password": "karthik098"
}
```
**Response:** JWT token string (plain text)

---

## 2. User Management

### Get All Users
`GET /api/users?status={status}`  
**Access:** ADMIN  
**Query Param (optional):** `status` — e.g., `Pending`, `Active`, `Inactive`, `Rejected`  
**Response:**
```json
[
  {
    "userID": 1,
    "name": "Karthik",
    "role": "ADMIN",
    "email": "karthik09@gmail.com",
    "phone": "9999888877",
    "status": "APPROVED"
  }
]
```

### Update User Status
`PUT /api/users/{userId}/status?adminId={adminId}`  
**Access:** ADMIN  
**Request Body:**
```json
{ "status": "Active" }
```
> Allowed values: `Active`, `Inactive`, `Rejected`

**Response:** Updated `UserResponse` object

---

## 3. Patient Management

### Register Patient
`POST /api/patients/register`  
**Access:** Public  
**Request Body:**
```json
{
  "name": "John Doe",
  "dob": "1995-06-15",
  "gender": "Male",
  "address": "123 Main Street, Chennai",
  "contactInfo": "9876543210",
  "email": "johndoe@example.com",
  "password": "password123"
}
```
> Patient status is set to `Pending` on creation. Admin must activate the account.

**Response:**
```json
{
  "patientID": 1,
  "userID": 11,
  "name": "John Doe",
  "dob": "1995-06-15",
  "gender": "Male",
  "address": "123 Main Street, Chennai",
  "contactInfo": "9876543210",
  "status": "Pending"
}
```

### Get Patient Profile
`GET /api/patients/{patientId}`  
**Access:** PATIENT (own), DOCTOR, ADMIN  
**Response:** `PatientResponse` object

### Update Patient Profile
`PUT /api/patients/{patientId}`  
**Access:** PATIENT (own), ADMIN  
**Request Body:**
```json
{
  "address": "456 New Street, Mumbai",
  "contactInfo": "9876543211"
}
```
**Response:** Updated `PatientResponse`

### Upload Patient Document
`POST /api/patients/documents`  
**Access:** PATIENT (own), ADMIN  
**Request Body:**
```json
{
  "patientID": 1,
  "docType": "IDProof",
  "fileURI": "https://storage.example.com/docs/idproof.pdf"
}
```
> `docType` must be `IDProof` or `HealthCard`

**Response:**
```json
{
  "documentID": 1,
  "patientID": 1,
  "docType": "IDProof",
  "fileURI": "https://storage.example.com/docs/idproof.pdf",
  "uploadedDate": "2025-01-01T10:00:00",
  "verificationStatus": "Pending"
}
```

### Get Medical History (Treatments)
`GET /api/patients/{patientId}/history`  
**Access:** PATIENT (own), DOCTOR, ADMIN  
**Response:** List of `TreatmentResponse`

### Get Medical Summary
`GET /api/patients/{patientId}/summary`  
**Access:** PATIENT (own), DOCTOR, ADMIN  
**Response:**
```json
{
  "recordID": 1,
  "patientID": 1,
  "patientName": "John Doe",
  "contactInfo": "9876543210",
  "detailsJSON": "| Diagnosis: Fever",
  "date": "2025-01-01",
  "status": "Active"
}
```

---

## 4. Hospital Management

### Create Hospital
`POST /api/hospitals`  
**Access:** ADMIN  
**Request Body:**
```json
{
  "name": "Apollo Hospital",
  "location": "Chennai",
  "capacity": 500,
  "status": "Active"
}
```

### Get All Hospitals
`GET /api/hospitals`  
**Access:** Any authenticated user

### Get Hospital by ID
`GET /api/hospitals/{id}`  
**Access:** Any authenticated user

### Search Hospitals
`GET /api/hospitals/search?query={query}`  
**Access:** Any authenticated user  
**Query Param:** `query` — searches by name or location

### Update Hospital
`PUT /api/hospitals/{id}`  
**Access:** ADMIN  
**Request Body:** Same as Create Hospital

### Delete Hospital
`DELETE /api/hospitals/{id}`  
**Access:** ADMIN  
**Response:** `204 No Content`

---

## 5. Resource Management

### Add Resource
`POST /api/resources`  
**Access:** ADMIN  
**Request Body:**
```json
{
  "hospitalID": 1,
  "type": "Beds",
  "quantity": 100,
  "status": "Available"
}
```
> `type` must be `Beds`, `Equipment`, or `Staff`

### Get All Resources
`GET /api/resources?hospitalId={hospitalId}`  
**Access:** Any authenticated user  
**Query Param (optional):** `hospitalId`

### Get Resource by ID
`GET /api/resources/{id}`  
**Access:** Any authenticated user

### Update Resource
`PUT /api/resources/{id}`  
**Access:** ADMIN  
**Request Body:** Same as Add Resource

### Delete Resource
`DELETE /api/resources/{id}`  
**Access:** ADMIN  
**Response:** `204 No Content`

---

## 6. Analytics

All analytics endpoints require **ADMIN** or **PROGRAM_MANAGER** role.

### Hospital Analytics Dashboard
`GET /api/analytics/hospitals`  
**Response:**
```json
{
  "totalHospitals": 5,
  "totalBeds": 1000,
  "totalEquipment": 300,
  "totalStaff": 500,
  "totalCapacity": 2500
}
```

### Hospital Capacity Report
`GET /api/analytics/reports/hospital-capacity`

### Resource Availability Report
`GET /api/analytics/reports/resource-availability`

### Resource Distribution Report
`GET /api/analytics/reports/resource-distribution`

---

## 7. Schedule Management

### Create Schedule Slot
`POST /api/schedules`  
**Access:** DOCTOR  
**Request Body:**
```json
{
  "doctorId": 1,
  "hospitalId": 1,
  "availableDate": "2025-08-01",
  "timeSlot": "10:00:00"
}
```

### Update Schedule Slot
`PUT /api/schedules/{scheduleId}`  
**Access:** DOCTOR  
**Request Body:** Same as Create Schedule Slot

### Get Doctor's Schedule
`GET /api/schedules/doctor/{doctorId}`  
**Access:** Any authenticated user

---

## 8. Appointment Management

### Book Appointment
`POST /api/appointments/book`  
**Access:** PATIENT  
**Request Body:**
```json
{
  "patientID": 1,
  "doctorID": 1,
  "hospitalID": 1,
  "date": "2025-08-01",
  "time": "10:00:00"
}
```
> A matching schedule slot with status `Available` must exist. Slot is marked `Booked` on success.

**Response:** `AppointmentResponse` with `status: "Confirmed"`

### Cancel Appointment
`PUT /api/appointments/cancel`  
**Access:** PATIENT (own appointments only)  
**Request Body:**
```json
{ "appointmentID": 1 }
```
> Schedule slot is automatically reverted to `Available`.

### Get Appointments by Doctor
`GET /api/appointments/doctor/{doctorId}`  
**Access:** DOCTOR, ADMIN

### Get All Appointments
`GET /api/appointments`  
**Access:** ADMIN

### Reassign Appointment
`PUT /api/appointments/{appointmentId}/reassign?adminId={adminId}`  
**Access:** ADMIN  
**Request Body:**
```json
{ "newDoctorId": 2 }
```

### Patient Check-In
`PUT /api/appointments/{appointmentId}/checkin?adminId={adminId}`  
**Access:** ADMIN  
**Response:** `AppointmentResponse` with `status: "Arrived"`

---

## 9. Treatment Management

### Record Treatment
`POST /api/treatments`  
**Access:** DOCTOR  
**Request Body:**
```json
{
  "patientId": 1,
  "doctorId": 1,
  "diagnosis": "Viral Fever",
  "prescription": "Paracetamol 500mg",
  "treatmentNotes": "Rest for 3 days",
  "status": "Active"
}
```
> `status` must be `Active` or `Completed`

**Response:** `TreatmentResponse`

### Update Patient Medical Record
`PUT /api/treatments/patients/{patientId}`  
**Access:** DOCTOR, ADMIN  
**Request Body:**
```json
{
  "updaterId": 1,
  "name": "John Doe",
  "contactInfo": "9876543210",
  "status": "Active"
}
```
> Cannot update if patient status is `Finalized`. Only Doctor or Admin can update.

---

## 10. Compliance Management

### Create Compliance Record
`POST /api/compliance?officerId={officerId}`  
**Access:** COMP_OFFICER  
**Request Body:**
```json
{
  "entityId": 1,
  "type": "Appointment",
  "result": "Pass",
  "notes": "All protocols followed"
}
```
> `type` must be `Appointment`, `Treatment`, or `Hospital`  
> `result` must be `Pass` or `Fail`

### Update Compliance Record
`PUT /api/compliance/{id}?officerId={officerId}`  
**Access:** COMP_OFFICER  
**Request Body:** Same as Create

### Search Compliance Records
`GET /api/compliance?type=&result=&entityId=&startDate=&endDate=`  
**Access:** COMP_OFFICER, ADMIN  
**All query params optional**

### Get Audit Logs
`GET /api/compliance/audit-logs`  
**Access:** COMP_OFFICER, ADMIN, AUDITOR  
**Response:** List of all `AuditLogResponse` entries

---

## 11. Audit Management

### Create Audit
`POST /api/audits`  
**Access:** AUDITOR  
**Request Body:**
```json
{
  "officerId": 9,
  "scope": "Hospital compliance review Q1",
  "findings": "Minor documentation gaps found",
  "status": "Scheduled"
}
```
> Default status on creation is `Scheduled`

### Update Audit
`PUT /api/audits/{auditId}?requesterId={requesterId}`  
**Access:** AUDITOR  
**Request Body:** Same as Create  
> Status transitions: `Scheduled → In-Progress → Completed`. Completed audits are read-only for non-auditors.

### Get All Audits
`GET /api/audits?status={status}`  
**Access:** AUDITOR, ADMIN  
**Query Param (optional):** `status`

### Get Audit by ID
`GET /api/audits/{auditId}`  
**Access:** AUDITOR, ADMIN

---

## 12. Notifications

### Get Notifications for User
`GET /api/notifications/{userId}`  
**Access:** Authenticated user (own notifications only; ADMIN can view any)  
**Response:**
```json
[
  {
    "notificationID": 1,
    "userId": 1,
    "entityId": 5,
    "message": "Your appointment on 2025-08-01 is confirmed.",
    "category": "Appointment",
    "status": "Unread",
    "createdDate": "2025-08-01T10:00:00"
  }
]
```

---

## 13. Program Management

Both endpoints require **ADMIN** or **PROGRAM_MANAGER** role.

### Program Dashboard
`GET /api/program/dashboard?startDate={date}&endDate={date}`  
**Query Params (optional):** `startDate`, `endDate` (format: `yyyy-MM-dd`)  
**Response:**
```json
{
  "totalAppointments": 120,
  "confirmedAppointments": 100,
  "cancelledAppointments": 20,
  "totalTreatments": 80,
  "activeTreatments": 30,
  "completedTreatments": 50,
  "totalHospitals": 5,
  "totalCapacity": 2500,
  "totalComplianceRecords": 40,
  "passedCompliance": 35,
  "failedCompliance": 5
}
```

### Generate Report
`POST /api/program/reports`  
**Request Body:**
```json
{
  "hospitalId": 1,
  "scope": "Appointment"
}
```
> `scope` must be `Appointment`, `Treatment`, `Hospital`, or `Compliance`

**Response:** `ReportResponse` with generated metrics JSON

---

## Error Responses

All errors follow this format:
```json
{
  "status": 400,
  "message": "Description of the error",
  "timestamp": 1700000000000
}
```

| HTTP Code | Meaning |
|---|---|
| 400 | Validation error / bad request |
| 401 | Missing or invalid JWT token |
| 403 | Insufficient role permissions |
| 404 | Resource not found |
| 409 | Duplicate record / constraint violation |
| 500 | Internal server error |

---

## Pre-Seeded Users (for Testing)

| Name | Email | Password | Role |
|---|---|---|---|
| Karthik | karthik09@gmail.com | karthik098 | ADMIN |
| Shashank | shashank09@gmail.com | shashank098 | ADMIN |
| Shiva | shiva09@gmail.com | shiva098 | DOCTOR |
| Akhil | akhil09@gmail.com | akhil098 | DOCTOR |
| Harshith | harshith09@gmail.com | harshith098 | PROGRAM_MANAGER |
| Krish | krish09@gmail.com | krish098 | PROGRAM_MANAGER |
| Mahendhar | mahendhar09@gmail.com | mahendhar098 | COMP_OFFICER |
| Priyank | priyank09@gmail.com | priyank098 | COMP_OFFICER |
| Vinay | vinay09@gmail.com | vinay098 | AUDITOR |
| Ayush | ayush09@gmail.com | ayush098 | AUDITOR |
